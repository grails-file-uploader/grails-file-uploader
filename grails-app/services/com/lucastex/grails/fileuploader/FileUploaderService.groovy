/*
 * Copyright (c) 2016, CauseCode Technologies Pvt Ltd, India.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or
 * without modification, are not permitted.
 */
package com.lucastex.grails.fileuploader

import com.lucastex.grails.fileuploader.cdn.CDNFileUploader
import com.lucastex.grails.fileuploader.cdn.google.GoogleCDNFileUploaderImpl
import grails.util.Holders
import groovy.io.FileType
import org.springframework.context.support.AbstractMessageSource
import org.springframework.web.multipart.commons.CommonsMultipartFile

import java.nio.channels.FileChannel

import javax.annotation.PostConstruct

import org.apache.commons.validator.UrlValidator

import com.lucastex.grails.fileuploader.cdn.BlobDetail
import com.lucastex.grails.fileuploader.cdn.amazon.AmazonCDNFileUploaderImpl
import com.lucastex.grails.fileuploader.util.Time

/**
 * A service class for all fileUpload related operations.
 */
@SuppressWarnings(['JavaIoPackageAccess', 'Instanceof'])
class FileUploaderService {

    private static final String HYPHEN = '-'
    private static String baseTemporaryDirectoryPath
    static final Map SAVE_FLUSH = [flush: true]
    private static final int THOUSAND = 1000
    private static final String SLASH = '/'
    private static final String UNDERSCORE = '_'

    def messageSource

    @PostConstruct
    void postConstruct() {
        baseTemporaryDirectoryPath = Holders.flatConfig['grails.tempDirectory'] ?: './temp'

        if (!baseTemporaryDirectoryPath.endsWith(SLASH)) {
            baseTemporaryDirectoryPath += SLASH
        }

        // Make sure directory exists
        File tempDirectory = new File(baseTemporaryDirectoryPath)
        tempDirectory.mkdirs()

        log.info "Temporary directory for file uploading [${tempDirectory.absolutePath}]"
    }

    /**
     * This method is used for dynamically instantiating the CDNFileUploader class based on the Provider.
     *
     * @param providerName The name of the provider.
     * @return Instance of the CDNFileUploader class.
     *
     * @author Nikhil Sharma
     * @since 2.4.9
     */
    CDNFileUploader getProviderInstance(String providerName) {
        String packageName = "com.lucastex.grails.fileuploader.cdn.${providerName.toLowerCase()}."
        String classNamePrefix = providerName.toLowerCase().capitalize()
        String providerClassName = packageName + "${classNamePrefix}CDNFileUploaderImpl"

        try {
            return Class.forName(providerClassName)?.newInstance()
        } catch (ClassNotFoundException e) {
            log.debug 'Could not find Provider class', e
            throw new ProviderNotFoundException("Provider $providerName not found.", e)
        }
    }

    String getNewTemporaryDirectoryPath() {
        String tempDirectoryPath = baseTemporaryDirectoryPath + UUID.randomUUID().toString() + SLASH
        File tempDirectory = new File(tempDirectoryPath)
        tempDirectory.mkdirs()

        // Delete the temporary directory when JVM exited
        tempDirectory.deleteOnExit()

        return tempDirectoryPath
    }

    /**
     * This method is used to save files to CDN providers.
     *
     * @param group
     * @param file
     * @param customFileName Custom file name without extension.
     * @return
     */
    UFile saveFile(String group, def file, String customFileName = '', Object userInstance = null,
        Locale locale = null) throws StorageConfigurationException, UploadFailureException, ProviderNotFoundException {

        Date expireOn
        long currentTimeMillis = System.currentTimeMillis()
        CDNProvider cdnProvider
        UFileType type = UFileType.LOCAL
        String path

        FileGroup fileGroupInstance = new FileGroup(group)
        Map fileGroupMap = fileGroupInstance.getFileNameAndExtensions(file, customFileName)

        if ((fileGroupMap.empty == true) || !file) {
            return null
        }

        try {
            fileGroupInstance.allowedExtensions(fileGroupMap, locale, group)
            fileGroupInstance.validateFileSize(fileGroupMap, locale)
        } catch (StorageConfigurationException storageConfigurationException) {
            throw storageConfigurationException
        }

        // If group specific storage type is not defined then use the common storage type
        String storageTypes = fileGroupInstance.groupConfig.storageTypes ?: fileGroupInstance.config.storageTypes

        if (storageTypes == 'CDN') {
            type = UFileType.CDN_PUBLIC

            try {
                fileGroupInstance.scopeFileName(userInstance, fileGroupMap, group, currentTimeMillis)
            } catch (StorageConfigurationException storageConfigurationException) {
                throw storageConfigurationException
            }
            long expirationPeriod = getExpirationPeriod(group)

            File tempFile

            if (file instanceof File) {
                // No need to transfer a file of type File since its already in a temporary location.
                // (Saves resource utilization)
                tempFile = file
            } else {
                if (file instanceof CommonsMultipartFile) {
                    tempFile = new File(newTemporaryDirectoryPath +
                            "${fileGroupMap.fileName}.${fileGroupMap.fileExtension}")

                    file.transferTo(tempFile)
                }
            }

            // Delete the temporary file when JVM exited since the base file is not required after upload
            tempFile.deleteOnExit()

            cdnProvider = fileGroupInstance.cdnProvider

            if (!cdnProvider) {
                throw new StorageConfigurationException('Provider not defined in the Config. Please define one.')
            }

            expireOn = isPublicGroup(group) ? null : new Date(new Date().time + expirationPeriod * THOUSAND)

            try {
                path = uploadFileToCloud(fileGroupMap, group, fileGroupInstance, tempFile)
            } catch (ProviderNotFoundException providerNotFoundException) {
                throw providerNotFoundException
            }
        } else {
            path = fileGroupInstance.getLocalSystemPath(storageTypes, fileGroupMap, currentTimeMillis)

            // Move file
            log.debug "Moving [$fileGroupMap.fileName] to [${path}]."
            moveFile(file, path)
        }

        UFile ufile = new UFile([name: fileGroupMap.fileName, size: fileGroupMap.fileSize, path: path, type: type,
                extension: fileGroupMap.fileExtension, expiresOn: expireOn, fileGroup: group, provider: cdnProvider])
        ufile.save()
        if (ufile.hasErrors()) {
            log.warn "Error saving UFile instance: $ufile.errors"
        }
        return ufile
    }

    /**
     * Method is used to upload file to cloud provider. Then it gets the path of uploaded file
     * @params fileGroupMap, group, fileGroupInstance, tempFile
     * @return path of uploaded file
     *
     */
    String uploadFileToCloud(Map fileGroupMap, String group, FileGroup fileGroupInstance, File tempFile) {
        CDNFileUploader fileUploaderInstance
        String path
        long expirationPeriod = getExpirationPeriod(group)
        String tempFileFullName = fileGroupMap.fileName + '.' + fileGroupMap.fileExtension
        Boolean makePublic = isPublicGroup(group)
        String containerName = fileGroupInstance.containerName

        try {
            fileUploaderInstance = getProviderInstance(fileGroupInstance.cdnProvider.name())
            fileUploaderInstance.uploadFile(containerName, tempFile, tempFileFullName, makePublic, expirationPeriod)

            if (makePublic) {
                path = fileUploaderInstance.getPermanentURL(containerName, tempFileFullName)
            } else {
                path = fileUploaderInstance.getTemporaryURL(containerName, tempFileFullName,
                        expirationPeriod)
            }
        } catch (ProviderNotFoundException providerNotFoundException) {
            throw providerNotFoundException
        } finally {
            fileUploaderInstance?.close()
        }
        return path
    }

    /**
     * Method is used to move file from temp directory to another.
     * @params fileInstance, path
     *
     */
    void moveFile(def file, String path) {
        if (file instanceof File) {
            file.renameTo(new File(path))
        }
        else {
            if (file instanceof CommonsMultipartFile) {
                file.transferTo(new File(path))
            }
        }
    }

    @SuppressWarnings('CatchException')
    boolean deleteFile(Serializable idUfile) {
        UFile ufile = UFile.get(idUfile)
        if (!ufile) {
            log.error "No UFile found with id: [$idUfile]"
            return false
        }

        try {
            ufile.delete()
        } catch (Exception e) {
            log.error "Could not delete ufile: ${idUfile}", e
            return false
        }

        return true
    }

    boolean deleteFileForUFile(UFile ufileInstance) throws ProviderNotFoundException, StorageException {
        log.debug "Deleting file for $ufileInstance"

        if (ufileInstance.type in [UFileType.CDN_PRIVATE, UFileType.CDN_PUBLIC]) {
            String containerName = ufileInstance.container

            if (ufileInstance.provider == CDNProvider.GOOGLE || ufileInstance.provider == CDNProvider.AMAZON) {
                CDNFileUploader fileUploaderInstance
                try {
                    fileUploaderInstance = getProviderInstance(ufileInstance.provider.name())
                    fileUploaderInstance.deleteFile(containerName, ufileInstance.fullName)
                } finally {
                    fileUploaderInstance?.close()
                }
            }
            return true
        }

        File file = new File(ufileInstance.path)
        if (!file.exists()) {
            log.warn "No file found at path [$ufileInstance.path] for ufile [$ufileInstance.id]."
            return false
        }
        File timestampFolder = file.parentFile

        if (file.delete()) {
            log.debug "File [${file?.path}] deleted."

            int numFilesInParentFolder = 0
            timestampFolder.eachFile(FileType.FILES) {
                numFilesInParentFolder ++
            }
            if (numFilesInParentFolder == 0) {
                timestampFolder.delete()
            } else {
                log.debug "Not deleting ${timestampFolder} as it contains files"
            }
        } else {
            log.error "Could not delete file: ${file}"
        }
    }

    /**
     * Access the Ufile, returning the appropriate message if the UFile does not exist.
     */
    UFile ufileById(Serializable idUfile, Locale locale) {
        UFile ufile = UFile.get(idUfile)

        if (ufile) {
            return ufile
        }
        String msg = messageSource.getMessage('fileupload.download.nofile', [idUfile] as Object[], locale)
        throw new FileNotFoundException(msg)
    }

    /**
     * Access the file held by the UFile, incrementing the viewed number, and returning appropriate message if
     * file does not exist.
     */
    File fileForUFile(UFile ufileInstance, Locale locale) {
        File file

        if (ufileInstance.type in [UFileType.CDN_PRIVATE, UFileType.CDN_PUBLIC]) {
            file = getFileFromURL(ufileInstance.path, ufileInstance.fullName)
        } else {
            file = new File(ufileInstance.path)
        }

        if (file.exists()) {
            // Increment the viewed number
            ufileInstance.downloads ++
            ufileInstance.save()
            return file
        }

        String msg = messageSource.getMessage('fileupload.download.filenotfound', [ufileInstance.name] as Object[],
                locale)
        throw new IOException(msg)
    }

    /**
     * Method to create a duplicate of an existing UFile
     * @param group
     * @param ufileInstance
     * @param name
     * @param locale
     * @throws StorageConfigurationException
     * @throws IOException
     */
    UFile cloneFile(String group, UFile ufileInstance, String name = '', Locale locale = null)
            throws StorageConfigurationException, IOException {
        log.info "Cloning ufile [${ufileInstance?.id}][${ufileInstance?.name}]"
        if (!ufileInstance) {
            log.warn 'Invalid/null ufileInstance received.'
            return null
        }

        String tempFile = newTemporaryDirectoryPath + (name ?: ufileInstance.fullName)

        File destFile = new File(tempFile)
        if (!destFile.exists()) {
            destFile.createNewFile()
        }

        String sourceFilePath = ufileInstance.path
        UrlValidator urlValidator = new UrlValidator()

        if (urlValidator.isValid(sourceFilePath) && ufileInstance.type != UFileType.LOCAL) {
            FileOutputStream fos = null

            try {
                fos = new FileOutputStream(destFile)
                fos.write(new URL(sourceFilePath).bytes)
            } finally {
                fos.close()
            }
        } else {
            File sourceFile = new File(sourceFilePath)
            FileChannel source = null
            FileChannel destination = null

            try {
                source = new FileInputStream(sourceFile).channel
                destination = new FileOutputStream(destFile).channel
                destination.transferFrom(source, 0, source.size())
            } finally {
                source?.close()
                destination?.close()
            }
        }

        return this.saveFile(group, destFile, name, null, locale)
    }

    String resolvePath(UFile ufileInstance) {
        if (!ufileInstance) {
            log.error 'No UFile instance found to resolve path'
            return ''
        }

        if (ufileInstance.type == UFileType.LOCAL) {
            return "/fileUploader/show/$ufileInstance.id"
        }
        if (ufileInstance.type == UFileType.CDN_PUBLIC) {
            return ufileInstance.path
        }
    }

    /**
     * This method is used to move files from Local server storage to Google cloud.
     * @params List of files to be moved
     *
     * @author Ankit Agrawal
     * @since 3.0.1
     *
     */
    List<Long> moveFilesToGoogleCloud(List<UFile> ufileInstanceList) {
        List<Long> failedUFileIdList = []
        long expirationPeriod
        CDNFileUploader fileUploaderInstance = new GoogleCDNFileUploaderImpl()

        List<BlobDetail> blobDetailList = getBlobDetailList(ufileInstanceList)

        blobDetailList.each {
            UFile uploadUFileInstance = it.ufile
            expirationPeriod = getExpirationPeriod(uploadUFileInstance.fileGroup)
            fileUploaderInstance.uploadFile(uploadUFileInstance.container, it.localFile, it.remoteBlobName, false,
                    expirationPeriod)

            failedUFileIdList = saveUploadedUFileInstance(it, uploadUFileInstance, fileUploaderInstance)
        }
        return failedUFileIdList
    }

    /**
     * This method is used to move files from Local server storage to Amazon cloud.
     * @params List of files to be moved
     *
     * @author Ankit Agrawal
     * @since 3.0.1
     *
     */
    List<Long> moveFilesToAmazonCloud(List<UFile> ufileInstanceList) {
        List<Long> failedUFileIdList = []
        CDNFileUploader fileUploaderInstance = new AmazonCDNFileUploaderImpl()
        long expirationPeriod

        List<BlobDetail> blobDetailList = getBlobDetailList(ufileInstanceList)


        blobDetailList.each {
            UFile uploadUFileInstance = it.ufile
            expirationPeriod = getExpirationPeriod(uploadUFileInstance.fileGroup)
            fileUploaderInstance.uploadFile(uploadUFileInstance.container, it.localFile, it.remoteBlobName,
                    false, expirationPeriod)

            failedUFileIdList = saveUploadedUFileInstance(it, uploadUFileInstance, fileUploaderInstance)
        }
        return failedUFileIdList
    }

    /**
     * This method saves details for UFile instances which were moved to CDN.
     * @params blobDetailInstance, uFileInstance, fileUploaderInstance
     * @return failedUFileIdList
     *
     * @author Ankit Agrawal
     * @since 3.0.1
     *
     */
    List<Long> saveUploadedUFileInstance(BlobDetail it, UFile uploadUFileInstance, CDNFileUploader fileUploaderInstance) {
        List<Long> failedUFileIdList = []
        if (uploadUFileInstance) {
            if (it.eTag) {
                uploadUFileInstance.name = it.remoteBlobName
                uploadUFileInstance.path = fileUploaderInstance.getPermanentURL(uploadUFileInstance.container,
                        it.remoteBlobName)
                uploadUFileInstance.type = UFileType.CDN_PRIVATE
                uploadUFileInstance.save(SAVE_FLUSH)
            } else {
                failedUFileIdList << it.ufile.id
            }
        } else {
            log.error 'Missing blobInstance. Never reach condition occured.'
        }
        return failedUFileIdList
    }

    /**
     * This method iterates on uFileInstanceList and assigns values to BlobDetail instances.
     * @params List of files to be moved
     * @return blobDetailList
     *
     * @author Ankit Agrawal
     * @since 3.0.1
     *
     */
    List<Long> getBlobDetailList(List<UFile> ufileInstanceList) {
        List<BlobDetail> blobDetailList = []

        ufileInstanceList.each {
            String fullName = it.fullName.trim().replaceAll(' ', UNDERSCORE).replaceAll(HYPHEN, UNDERSCORE)
            String newFileName = "${it.fileGroup}-${System.currentTimeMillis()}-${fullName}"
            blobDetailList << new BlobDetail(newFileName, new File(it.path), it)
            Thread.sleep(2)
        }

        return blobDetailList
    }

    void renewTemporaryURL(boolean forceAll = false) {
        String expiresOnString = 'expiresOn'
        CDNProvider.values().each { CDNProvider cdnProvider ->
            if (cdnProvider == CDNProvider.RACKSPACE) {
                return
            }

            CDNFileUploader fileUploaderInstance = getProviderInstance(cdnProvider.name())

            if (!fileUploaderInstance) {
                return
            }

            UFile.withCriteria {
                eq('type', UFileType.CDN_PUBLIC)
                eq('provider', cdnProvider)

                if (Holders.flatConfig['fileuploader.persistence.provider'] == 'mongodb') {
                    eq(expiresOnString, [$exists: true])
                } else {
                    isNotNull(expiresOnString)
                }
                if (!forceAll) {
                    or {
                        lt(expiresOnString, new Date())
                        // Getting all CDN UFiles which are about to expire within one day.
                        between(expiresOnString, new Date(), new Date() + 1)
                    }
                }
            }.each { UFile ufileInstance ->
                log.debug "Renewing URL for $ufileInstance"

                String containerName = ufileInstance.container
                String fileFullName = ufileInstance.fullName
                long expirationPeriod = getExpirationPeriod(ufileInstance.fileGroup)

                ufileInstance.path = fileUploaderInstance.getTemporaryURL(containerName, fileFullName, expirationPeriod)
                ufileInstance.expiresOn = new Date(new Date().time + expirationPeriod * THOUSAND)
                ufileInstance.save(SAVE_FLUSH)
                if (ufileInstance.hasErrors()) {
                    log.debug "Error saving new URL for $ufileInstance"
                }

                log.debug "New URL for $ufileInstance [$ufileInstance.path] [$ufileInstance.expiresOn]"
            }

            fileUploaderInstance.close()
        }
    }

    long getExpirationPeriod(String fileGroup) {
        // Default to 30 Days
        return Holders.flatConfig["fileuploader.groups.${fileGroup}.expirationPeriod"] ?: (Time.DAY * 30)
    }

    /**
     * Retrieves content of the given url and stores it in the temporary directory.
     *
     * @param url The URL from which file to be retrieved
     * @param filename Name of the file
     */
    File getFileFromURL(String url, String filename) {
        String path = newTemporaryDirectoryPath

        File file = new File(path + filename)
        FileOutputStream fos = new FileOutputStream(file)
        try {
            fos.write(new URL(url).bytes)
        } catch (FileNotFoundException e) {
            log.info "URL ${url} not found"
        }
        fos.close()

        // Delete the temporary file when JVM exited since the base file is not required after upload
        file.deleteOnExit()

        return file
    }

    /**
     * This method is used to update meta data of all the previously uploaded files to the
     * {@link CDNProvider CDNProvider} bucket. Currently only Amazon is supported.
     * @param {@link CDNProvider CDNProvider}
     * @since 2.4.3
     * @author Priyanshu Chauhan
     */
    void updateAllUFileCacheHeader(CDNProvider cdnProvider = CDNProvider.AMAZON) {
        AmazonCDNFileUploaderImpl amazonFileUploaderInstance = new AmazonCDNFileUploaderImpl()
        amazonFileUploaderInstance.authenticate()

        // TODO: Add support for Rackspace
        if (cdnProvider != CDNProvider.AMAZON) {
            log.warn "Only AMAZON is allowed for updating cache header not $cdnProvider"
            return
        }

        UFile.withCriteria {
            eq('type', UFileType.CDN_PUBLIC)
            eq('provider', cdnProvider)
        }.each { UFile uFileInstance ->
            Boolean makePublic = isPublicGroup(uFileInstance.fileGroup)
            long expirationPeriod = getExpirationPeriod(uFileInstance.fileGroup)

            amazonFileUploaderInstance.updatePreviousFileMetaData(uFileInstance.container,
                    uFileInstance.fullName, makePublic, expirationPeriod)
        }
        amazonFileUploaderInstance.close()
    }

    /**
     * This method is used to check whether the provided group is of public type or not.
     * @param fileGroup
     * @return Boolean result
     * @since 2.4.3
     * @author Priyanshu Chauhan
     */
    Boolean isPublicGroup(String fileGroup) {
        return Holders.flatConfig["fileuploader.groups.${fileGroup}.makePublic"] ? true : false
    }

    /**
     * Moves file from CDN provider to other, updates UFile path. Needs to be executed only once.
     * @param CDNProvider target CDN Provider enum
     * @param String CDN Container name
     * @param boolean true or false if move was successful
     * @author Rohit Pal
     */
    boolean moveToNewCDN(CDNProvider toCDNProvider, String containerName, boolean makePublic = false) {
        if (!toCDNProvider || !containerName) {
            return false
        }
        moveFilesToCDN(toCDNProvider, containerName, makePublic, UFile.findAllByTypeNotEqual(UFileType.LOCAL))
        return true
    }

    /**
     * Moves file from CDN provider to other, updates UFile path. Needs to be executed only once.
     * @param CDNProvider target CDN Provider enum
     * @param String CDN Container name
     * @param boolean true or false if move was successful
     * @param List UFile list. File to be moved
     * @author Rohit Pal
     */
    void moveFilesToCDN(CDNProvider toCDNProvider, String containerName, boolean makePublic = false,
            List<UFile> uFileList) throws ProviderNotFoundException, StorageException {
        String filename, savedUrlPath
        String message = 'Moved successfully'
        File downloadedFile
        boolean isSuccess = true

        uFileList
            .findAll { it.provider != toCDNProvider }
            .each { uFile ->
                filename = uFile.name
                filename = filename.contains(SLASH) ? filename[(filename.lastIndexOf(SLASH) + 1)..-1] : filename
                downloadedFile = getFileFromURL(uFile.path, filename)

                UFileMoveHistory uFileHistory = UFileMoveHistory.findOrCreateByUfile(uFile)

                if (!downloadedFile.exists()) {
                    log.debug "Downloaded file doesn't not exist."
                    return
                }

                long expirationPeriod = getExpirationPeriod(uFile.fileGroup)

                try {
                    if (toCDNProvider == CDNProvider.GOOGLE || toCDNProvider == CDNProvider.AMAZON) {
                        CDNFileUploader fileUploaderInstance
                        try {
                            fileUploaderInstance = getProviderInstance(toCDNProvider.name())
                            fileUploaderInstance.uploadFile(containerName, downloadedFile, uFile.fullName, makePublic,
                                    expirationPeriod)

                            if (makePublic) {
                                savedUrlPath = fileUploaderInstance.getPermanentURL(containerName, uFile.fullName)
                            } else {
                                savedUrlPath = fileUploaderInstance.getTemporaryURL(containerName, uFile.fullName,
                                        expirationPeriod)
                            }
                        } finally {
                            fileUploaderInstance?.close()
                        }
                    }
                } catch (Exception e) {
                    isSuccess = false
                    message = e.message
                    log.debug message
                }

                uFileHistory.moveCount++
                uFileHistory.lastUpdated = new Date()
                uFileHistory.toCDN = toCDNProvider
                uFileHistory.fromCDN = uFile.provider
                uFileHistory.details = message

                if (isSuccess) {
                    log.debug "File moved: ${filename}"

                    uFileHistory.status = MoveStatus.SUCCESS
                    uFile.path = savedUrlPath
                    uFile.provider = toCDNProvider
                    uFile.expiresOn = new Date(new Date().time + expirationPeriod * THOUSAND)

                    if (makePublic) {
                        uFile.type = UFileType.CDN_PUBLIC
                    }
                    uFile.save(SAVE_FLUSH)
                } else {
                    log.debug "Error in moving file: ${filename}"
                    uFileHistory.status = MoveStatus.FAILURE
                }
                uFile.save(SAVE_FLUSH)
                uFileHistory.save(SAVE_FLUSH)
            }

        reSubmitForFailedFiles(toCDNProvider, containerName, makePublic)
    }

    // Re-submitting UFile for failed files
    void reSubmitForFailedFiles(CDNProvider toCDNProvider, String containerName, boolean makePublic) {
        List<UFile> failureFileList = UFileMoveHistory.withCriteria {
            and {
                eq('status', MoveStatus.FAILURE)
                le('moveCount', 3)
            }
            projections {
                property('ufile')
            }
        }

        if (failureFileList.size() > 0) {
            moveFilesToCDN(toCDNProvider, containerName, makePublic, failureFileList)
        }
    }
}
