/*
 * Copyright (c) 2016, CauseCode Technologies Pvt Ltd, India.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or
 * without modification, are not permitted.
 */
package com.causecode.fileuploader

import com.causecode.fileuploader.cdn.CDNFileUploader
import com.causecode.fileuploader.util.FileUploaderUtils
import com.causecode.util.NucleusUtils
import grails.core.GrailsApplication
import grails.util.Holders
import groovy.io.FileType
import org.springframework.context.MessageSource
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.web.multipart.commons.CommonsMultipartFile
import java.nio.channels.FileChannel
import org.apache.commons.validator.UrlValidator
import com.causecode.fileuploader.cdn.amazon.AmazonCDNFileUploaderImpl
import com.causecode.fileuploader.util.Time

/**
 * A service class for all fileUpload related operations.
 */
@SuppressWarnings(['JavaIoPackageAccess', 'Instanceof'])
class FileUploaderService {

    MessageSource messageSource
    GrailsApplication grailsApplication

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
        String packageName = "com.causecode.fileuploader.cdn.${providerName.toLowerCase()}."
        String classNamePrefix = providerName.toLowerCase().capitalize()
        String providerClassName = packageName + "${classNamePrefix}CDNFileUploaderImpl"

        try {
            return grailsApplication.classLoader.loadClass(providerClassName)?.newInstance()
        } catch (ClassNotFoundException e) {
            log.debug 'Could not find Provider class', e
            throw new ProviderNotFoundException("Provider $providerName not found.", e)
        }
    }

    String getNewTemporaryDirectoryPath() {
        String tempDirectoryPath = FileUploaderUtils.baseTemporaryDirectoryPath + UUID.randomUUID().toString() + '/'
        File tempDirectory = new File(tempDirectoryPath)
        tempDirectory.mkdirs()

        // Delete the temporary directory when JVM exited
        tempDirectory.deleteOnExit()

        return tempDirectoryPath
    }

    /**
     * This method is used to save files locally or to CDN providers.
     *
     * @param group
     * @param file
     * @param customFileName Custom file name without extension.
     * @return
     */
    UFile saveFile(String group, def file, String customFileName = '', Object userInstance = null, Locale locale = null)
            throws StorageConfigurationException, UploadFailureException, ProviderNotFoundException {

        Date expireOn
        long currentTimeMillis = System.currentTimeMillis()
        CDNProvider cdnProvider
        UFileType type = UFileType.LOCAL
        String path

        FileGroup fileGroupInstance = new FileGroup(group)
        Map fileData = fileGroupInstance.getFileNameAndExtensions(file, customFileName)

        if (fileData.empty || !file) {
            return null
        }

        fileGroupInstance.allowedExtensions(fileData, locale, group)
        fileGroupInstance.validateFileSize(fileData, locale)

        // If group specific storage type is not defined then use the common storage type
        String storageTypes = fileGroupInstance.groupConfig.storageTypes ?: fileGroupInstance.config.storageTypes

        if (storageTypes == 'CDN') {
            type = UFileType.CDN_PUBLIC

            fileGroupInstance.scopeFileName(userInstance, fileData, group, currentTimeMillis)
            long expirationPeriod = getExpirationPeriod(group)

            File tempFile

            if (file instanceof File) {
                /* No need to transfer a file of type File since its already in a temporary location.
                * (Saves resource utilization)
                */
                tempFile = file
            } else {
                if (file instanceof CommonsMultipartFile) {
                    tempFile = new File(newTemporaryDirectoryPath +
                            "${fileData.fileName}.${fileData.fileExtension}")

                    file.transferTo(tempFile)
                }
            }

            // Delete the temporary file when JVM exited since the base file is not required after upload
            tempFile.deleteOnExit()

            cdnProvider = fileGroupInstance.cdnProvider

            if (!cdnProvider) {
                throw new StorageConfigurationException('Provider not defined in the Config. Please define one.')
            }

            expireOn = isPublicGroup(group) ? null : new Date(new Date().time + expirationPeriod * 1000)

            path = uploadFileToCloud(fileData, fileGroupInstance, tempFile)

        } else {
            path = fileGroupInstance.getLocalSystemPath(storageTypes, fileData, currentTimeMillis)

            // Move file
            log.debug "Moving [$fileData.fileName] to [${path}]."
            moveFile(file, path)
        }

        UFile ufile = new UFile([name: fileData.fileName, size: fileData.fileSize, path: path, type: type,
                extension: fileData.fileExtension, expiresOn: expireOn, fileGroup: group, provider: cdnProvider])
        NucleusUtils.save(ufile, true)

        return ufile
    }

    /**
     * Method is used to upload file to cloud provider. Then it gets the path of uploaded file
     * @params fileData, fileGroupInstance, tempFile
     * @return path of uploaded file
     *
     */
    String uploadFileToCloud(Map fileData, FileGroup fileGroupInstance, File tempFile) {
        CDNFileUploader fileUploaderInstance
        String path
        long expirationPeriod = getExpirationPeriod(fileGroupInstance.groupName)
        String tempFileFullName = fileData.fileName + '.' + fileData.fileExtension
        Boolean makePublic = isPublicGroup(fileGroupInstance.groupName)
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
        } else {
            if (file instanceof CommonsMultipartFile) {
                file.transferTo(new File(path))
            }
        }
    }

    boolean deleteFile(Serializable idUfile) {
        UFile ufile = UFile.get(idUfile)
        if (!ufile) {
            log.error "No UFile found with id: [$idUfile]"
            return false
        }

        try {
            ufile.delete()
        } catch (DataIntegrityViolationException e) {
            log.error "Could not delete ufile: ${idUfile}", e
            return false
        }

        return true
    }

    boolean deleteFileForUFile(UFile ufileInstance) throws ProviderNotFoundException, StorageException {
        log.debug "Deleting file for $ufileInstance"

        if (ufileInstance.type == UFileType.CDN_PRIVATE || ufileInstance.type == UFileType.CDN_PUBLIC) {

            CDNFileUploader fileUploaderInstance
            try {
                fileUploaderInstance = getProviderInstance(ufileInstance.provider.name())
                fileUploaderInstance.deleteFile(ufileInstance.container, ufileInstance.fullName)
            } finally {
                fileUploaderInstance?.close()
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
                numFilesInParentFolder++
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

        if (ufileInstance.type == UFileType.CDN_PRIVATE || ufileInstance.type == UFileType.CDN_PUBLIC) {
            file = getFileFromURL(ufileInstance.path, ufileInstance.fullName)
        } else {
            file = new File(ufileInstance.path)
        }

        if (file.exists()) {
            // Increment the viewed number
            ufileInstance.downloads++
            NucleusUtils.save(ufileInstance, true)
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

        if (!ufileInstance) {
            log.warn 'Invalid/null ufileInstance received.'
            return null
        }

        log.info "Cloning ufile [${ufileInstance.id}][${ufileInstance.name}]"

        String tempFile = newTemporaryDirectoryPath + (name ?: ufileInstance.fullName)

        File destFile = new File(tempFile)
        if (!destFile.exists()) {
            destFile.createNewFile()
        }

        String sourceFilePath = ufileInstance.path
        UrlValidator urlValidator = new UrlValidator()

        if (urlValidator.isValid(sourceFilePath) && ufileInstance.type != UFileType.LOCAL) {
            FileOutputStream fileOutputStream = null

            try {
                fileOutputStream = new FileOutputStream(destFile)
                fileOutputStream.write(new URL(sourceFilePath).bytes)
            } finally {
                fileOutputStream.close()
            }
        } else {
            File sourceFile = new File(sourceFilePath)
            FileChannel source
            FileChannel destination

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
            return "/file-uploader/show/$ufileInstance.id"
        }
        if (ufileInstance.type == UFileType.CDN_PUBLIC || ufileInstance.type == UFileType.CDN_PRIVATE) {
            return ufileInstance.path
        }
    }

    void renewTemporaryURL(boolean forceAll = false) {
        CDNProvider.values().each { CDNProvider cdnProvider ->
            if (cdnProvider == CDNProvider.RACKSPACE || cdnProvider == CDNProvider.LOCAL) {
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
                    eq('expiresOn', [$exists: true])
                } else {
                    isNotNull('expiresOn')
                }
                if (!forceAll) {
                    or {
                        lt('expiresOn', new Date())
                        // Getting all CDN UFiles which are about to expire within one day.
                        between('expiresOn', new Date(), new Date() + 1)
                    }
                }
                maxResults(100)
            }.each { UFile ufileInstance ->
                log.debug "Renewing URL for $ufileInstance"

                long expirationPeriod = getExpirationPeriod(ufileInstance.fileGroup)

                ufileInstance.path = fileUploaderInstance.getTemporaryURL(ufileInstance.container,
                        ufileInstance.fullName, expirationPeriod)
                ufileInstance.expiresOn = new Date(new Date().time + expirationPeriod * 1000)
                NucleusUtils.save(ufileInstance, true)
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
        FileOutputStream fileOutputStream = new FileOutputStream(file)
        try {
            fileOutputStream.write(new URL(url).bytes)
        } catch (FileNotFoundException e) {
            log.info "URL ${url} not found"
        }
        fileOutputStream.close()

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

        if (cdnProvider != CDNProvider.AMAZON) {
            log.warn "Only AMAZON is allowed for updating cache header not $cdnProvider"
            return
        }

        UFile.withCriteria {
            eq('type', UFileType.CDN_PUBLIC)
            eq('provider', cdnProvider)
            maxResults(100)
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
     * Moves all UFiles stored at any CDN provider to the given CDN provider. Does not touch UFiles stored locally.
     * Needs to be executed only once.
     * @param CDNProvider target CDN Provider enum
     * @param String CDN Container name
     * @param boolean true or false if move was successful
     * @author Rohit Pal
     */
    boolean moveToNewCDN(CDNProvider toCDNProvider, String containerName, boolean makePublic = false) {
        if (!toCDNProvider || !containerName) {
            return false
        }
        moveFilesToCDN(UFile.findAllByTypeNotEqual(UFileType.LOCAL), toCDNProvider, makePublic)
        return true
    }

    /**
     * Moves files saved locally or from one CDN provider to another CDN provider, updates UFile path.
     * Needs to be executed only once.
     * @param CDNProvider target CDN Provider enum
     * @param String CDN Container name
     * @param boolean true or false if move was successful
     * @param List UFile list. File to be moved
     * @author Rohit Pal
     */
    // TODO Refactor this method and move iterations on individual UFiles to a new method.
    @SuppressWarnings(['CatchException'])
    def moveFilesToCDN(List<UFile> uFileList, CDNProvider toCDNProvider,
                       boolean makePublic = false) throws ProviderNotFoundException, StorageException {
        String fileName, savedUrlPath
        String message = 'Moved successfully'
        File downloadedFile
        boolean isSuccess = true
        List<UFile> uFileUploadFailureList = []

        uFileList
            .findAll { it.provider != toCDNProvider || it.type == UFileType.LOCAL }
            .each { uFile ->
                fileName = getNewFileNameFromUFile(uFile)

                if (uFile.type == UFileType.LOCAL) {
                    downloadedFile = new File(uFile.path)
                } else {
                    if (uFile.type == UFileType.CDN_PRIVATE || uFile.type == UFileType.CDN_PUBLIC) {
                        downloadedFile = getFileFromURL(uFile.path, uFile.name)
                    }
                }

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
                        fileUploaderInstance.uploadFile(uFile.container, downloadedFile, fileName, makePublic,
                                expirationPeriod)

                        if (makePublic) {
                            savedUrlPath = fileUploaderInstance.getPermanentURL(uFile.container, fileName)
                        } else {
                            savedUrlPath = fileUploaderInstance.getTemporaryURL(uFile.container, fileName,
                                    expirationPeriod)
                        }
                    } finally {
                        fileUploaderInstance?.close()
                    }
                }
            } catch (Exception e) {
                isSuccess = false
                message = e.message
                log.debug message, e
            }

            UFileMoveHistory uFileHistory = UFileMoveHistory.findOrCreateByUfile(uFile)
            uFileHistory.moveCount++
            uFileHistory.lastUpdated = new Date()
            uFileHistory.toCDN = toCDNProvider
            uFileHistory.fromCDN = uFile.provider ?: CDNProvider.LOCAL
            uFileHistory.details = message

            if (isSuccess) {
                log.debug "File moved: ${uFile.name}"

                uFileHistory.status = MoveStatus.SUCCESS
                uFile.name = fileName
                uFile.path = savedUrlPath
                uFile.provider = toCDNProvider
                uFile.expiresOn = new Date(new Date().time + expirationPeriod * 1000)
                uFile.type = makePublic ? UFileType.CDN_PUBLIC : UFileType.CDN_PRIVATE

                NucleusUtils.save(uFile, true)
            } else {
                log.debug "Error in moving file: ${fileName}"
                uFileHistory.status = MoveStatus.FAILURE
                uFileUploadFailureList << uFile
            }
            NucleusUtils.save(uFileHistory, true)
        }

        return uFileUploadFailureList
    }

    /**
     * Method fetches instances of failed CDN uploads from UFileMoveHistory and tries to upload them to desired
     * CDNProvider.
     * This method is called from a DailyJob.
     *
     */
    // TODO Remove repeated queries and enable uploads for each UFile in list.
    void moveFailedFilesToCDN() {
        List failureFileList = UFileMoveHistory.withCriteria {
            eq('status', MoveStatus.FAILURE)
            maxResults(200)
        }

        List failureFileListForGoogleCDN = failureFileList.findAll { it.toCDN == CDNProvider.GOOGLE }*.ufile
        List failureFileListForAmazonCDN = failureFileList.findAll { it.toCDN == CDNProvider.AMAZON }*.ufile

        moveFilesToCDN(failureFileListForGoogleCDN, CDNProvider.GOOGLE)
        moveFilesToCDN(failureFileListForAmazonCDN, CDNProvider.AMAZON)
    }

    /**
     * Method uses fullName of a UFile to construct a new fileName.
     * @params UFile uFile
     * @return String fileName
     *
     */
    String getNewFileNameFromUFile(UFile uFile) {
        String fullName
        fullName = uFile.fullName.trim().replaceAll(' ', '_').replaceAll('-', '_')
        fullName = fullName.contains('/') ? fullName[(fullName.lastIndexOf('/') + 1)..-1] : fullName

        return "${uFile.fileGroup}-${System.currentTimeMillis()}-${fullName}"
    }
}
