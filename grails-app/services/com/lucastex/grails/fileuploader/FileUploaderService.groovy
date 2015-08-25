package com.lucastex.grails.fileuploader

import grails.util.Holders
import groovy.io.FileType
import groovy.time.TimeCategory
import groovy.time.TimeDuration

import java.nio.channels.FileChannel

import javax.annotation.PostConstruct

import org.apache.commons.validator.UrlValidator
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.commons.CommonsMultipartFile

import com.lucastex.grails.fileuploader.cdn.BlobDetail
import com.lucastex.grails.fileuploader.cdn.amazon.AmazonCDNFileUploaderImpl
import com.lucastex.grails.fileuploader.util.Time

class FileUploaderService {

    private static final String FILE_NAME_SEPARATOR = "-"
    private static String baseTemporaryDirectoryPath

    static transactional = false

    def messageSource
    def rackspaceCDNFileUploaderService

    @PostConstruct
    void postConstruct() {
        baseTemporaryDirectoryPath = Holders.getFlatConfig()["grails.tempDirectory"] ?: "./temp"

        if (!baseTemporaryDirectoryPath.endsWith("/")) {
            baseTemporaryDirectoryPath += "/"
        }

        // Make sure directory exists
        File tempDirectory = new File(baseTemporaryDirectoryPath)
        tempDirectory.mkdirs()

        log.info "Temporary directory for file uploading [${tempDirectory.absolutePath}"
    }

    String getNewTemporaryDirectoryPath() {
        String tempDirectoryPath = baseTemporaryDirectoryPath + UUID.randomUUID().toString() + "/"
        File tempDirectory = new File(tempDirectoryPath)
        tempDirectory.mkdirs()

        // Delete the temporary directory when JVM exited
        tempDirectory.deleteOnExit()
        
        println "<<<tempDirectoryPath: $tempDirectoryPath"

        return tempDirectoryPath
    }

    /**
     * @param group
     * @param file
     * @param customFileName Custom file name without extension.
     * @return
     */
    @Transactional
    UFile saveFile(String group, def file, String customFileName = "", Object userInstance = null,
            Locale locale = null) throws FileUploaderServiceException {

        Long fileSize
        Date expireOn
        String permanentURL
        boolean empty = true
        long currentTimeMillis = System.currentTimeMillis()
        CDNProvider cdnProvider
        UFileType type = UFileType.LOCAL
        String contentType, fileExtension, fileName, path, receivedFileName

        if (file instanceof File) {
            contentType = ""
            empty = !file.exists()
            receivedFileName = file.name
            fileSize = file.size()
        } else {    // Means instance is of Spring's CommonsMultipartFile.
            CommonsMultipartFile uploaderFile = file
            contentType = uploaderFile?.contentType
            empty = uploaderFile?.isEmpty()
            receivedFileName = uploaderFile?.originalFilename
            fileSize = uploaderFile?.size
        }

        log.info "Received ${empty ? 'empty ' : ''}file [$receivedFileName] of size [$fileSize] & content type [$contentType]."
        if (empty || !file) {
            return null
        }

        ConfigObject config = Holders.getConfig().fileuploader
        ConfigObject groupConfig = config[group]

        if (config[group].isEmpty()) {
            throw new FileUploaderServiceException("No config defined for group [$group]. Please define one in your Config file.")
        }

        int extensionAt = receivedFileName.lastIndexOf(".")
        if (extensionAt > -1) {
            fileName = customFileName ?: receivedFileName.substring(0, extensionAt)
            fileExtension = receivedFileName.substring(extensionAt + 1).toLowerCase().trim()
        } else {
            fileName = customFileName ?: receivedFileName
        }

        if (!groupConfig.allowedExtensions[0].equals("*") && !groupConfig.allowedExtensions.contains(fileExtension)) {
            String msg = messageSource.getMessage("fileupload.upload.unauthorizedExtension",
                    [fileExtension, groupConfig.allowedExtensions] as Object[], locale)
            log.debug msg
            throw new FileUploaderServiceException(msg)
        }

        /**
         * If maxSize config exists
         */
        if (groupConfig.maxSize) {
            def maxSizeInKb = ((int) (groupConfig.maxSize)) / 1024
            if (fileSize > groupConfig.maxSize) { //if filesize is bigger than allowed
                log.debug "FileUploader plugin received a file bigger than allowed. Max file size is ${maxSizeInKb} kb"
                def msg = messageSource.getMessage("fileupload.upload.fileBiggerThanAllowed", [maxSizeInKb] as Object[], locale)
                throw new FileUploaderServiceException(msg)
            }
        }

        /**
         * Convert all white space to underscore and hyphens to underscore to differentiate
         * different data on filename created below.
         */
        customFileName = customFileName.trim().replaceAll(" ", "_").replaceAll("-", "_")

        // If group specific storage type is not defined then use the common storage type
        String storageTypes = groupConfig.storageTypes ?: config.storageTypes

        if (storageTypes == "CDN") {
            type = UFileType.CDN_PUBLIC
            String containerName

            // If group specific container is not defined
            if (groupConfig.container instanceof ConfigObject) {
                // Then use the common container name
                containerName = UFile.containerName(config.container)
            } else {
                // Otherwise use the group specific container
                containerName = UFile.containerName(groupConfig.container)
            }

            long expirationPeriod = getExpirationPeriod(group)

            StringBuilder fileNameBuilder = new StringBuilder(group)
                    .append(FILE_NAME_SEPARATOR)

            if (userInstance && userInstance.id) {
                fileName.append(userInstance.id.toString())
                fileName.append(FILE_NAME_SEPARATOR)
            }

            fileNameBuilder.append(currentTimeMillis)
                    .append(FILE_NAME_SEPARATOR)
                    .append(fileName)

            /**
             * Generating file names like:
             * 
             * @example When userInstance available:
             * avatar-14-1415804444014-myavatar.png
             * 
             * @example When userInstance is not available:
             * logo-1415804444014-organizationlogo.png
             *  
             */
            fileName = fileNameBuilder.toString()

            String tempFileFullName = fileName + "." + fileExtension

            File tempFile

            if (file instanceof File) {
                // No need to transfer a file of type File since its already in a temporary location.
                // (Saves resource utilization)
                tempFile = file
            } else {
                tempFile = new File(getNewTemporaryDirectoryPath() + "${fileName}.${fileExtension}")

                file.transferTo(tempFile)
            }

            // Delete the temporary file when JVM exited since the base file is not required after upload
            tempFile.deleteOnExit()

            if (groupConfig.provider instanceof ConfigObject) {
                cdnProvider = config.provider
            } else {
                cdnProvider = groupConfig.provider
            }

            expireOn = new Date(new Date().time + expirationPeriod * 1000)

            if (cdnProvider == CDNProvider.AMAZON) {
                AmazonCDNFileUploaderImpl amazonFileUploaderInstance = AmazonCDNFileUploaderImpl.getInstance()
                amazonFileUploaderInstance.authenticate()
                long maxAge = getMaxAge(expireOn)
                amazonFileUploaderInstance.uploadFile(containerName, tempFile, tempFileFullName, true, maxAge)
                path = amazonFileUploaderInstance.getTemporaryURL(containerName, tempFileFullName, expirationPeriod)
                permanentURL =  amazonFileUploaderInstance.getPermanentURL(containerName, tempFileFullName)
                
                amazonFileUploaderInstance.close()
            } else {
                String publicBaseURL = rackspaceCDNFileUploaderService.uploadFileToCDN(containerName, tempFile, tempFileFullName)
                path = publicBaseURL + "/" + tempFileFullName
            }
        } else {
            // Base path to save file
            path = groupConfig.path
            if (!path.endsWith('/')) path = path + "/";

            if (storageTypes?.contains('monthSubdirs')) {  //subdirectories by month and year
                Calendar cal = Calendar.getInstance()
                path = path + cal[Calendar.YEAR].toString() + cal[Calendar.MONTH].toString() + '/'
            } else {  //subdirectories by millisecond
                path = path + currentTimeMillis + "/"
            }

            // Make sure the directory exists
            if (! new File(path).exists()) {
                if (!new File(path).mkdirs()) {
                    log.error "FileUploader plugin couldn't create directories: [${path}]"
                }
            }

            // If using the uuid storage type
            if (storageTypes?.contains('uuid')) {
                path = path + UUID.randomUUID().toString()
            } else {  //note:  this type of storage is a bit of a security / data loss risk.
                path = path + fileName + "." + fileExtension
            }

            // Move file
            log.debug "Moving [$fileName] to [${path}]."
            if (file instanceof File)
                file.renameTo(new File(path))
            else
                file.transferTo(new File(path))
        }

        UFile ufile = new UFile()
        ufile.name = fileName
        ufile.size = fileSize
        ufile.extension = fileExtension
        ufile.path = path
        ufile.type = type
        ufile.expiresOn = expireOn
        ufile.fileGroup = group
        ufile.provider = cdnProvider
        
        
        ufile.permanentURL = permanentURL
//        ufile.permanentURL = ufile.generatePermanentURL()
        println "<><><><> permanentURL: ${permanentURL}"
        ufile.save()
        if (ufile.hasErrors()) {
            log.warn "Error saving UFile instance: $ufile.errors"
        }
        return ufile
    }

    @Transactional
    boolean deleteFile(Serializable idUfile) {
        UFile ufile = UFile.get(idUfile)
        if (!ufile) {
            log.error "No UFile found with id: [$idUfile]"
            return false
        }
        File file = new File(ufile.path)

        try {
            ufile.delete()
        } catch(Exception e) {
            log.error "Could not delete ufile: ${idUfile}", e
            return false
        }

        return true
    }

    boolean deleteFileForUFile(UFile ufileInstance) {
        log.debug "Deleting file for $ufileInstance"

        if (ufileInstance.type in [UFileType.CDN_PRIVATE, UFileType.CDN_PUBLIC]) {
            String containerName = ufileInstance.container

            if (ufileInstance.provider == CDNProvider.AMAZON) {
                AmazonCDNFileUploaderImpl amazonFileUploaderInstance = AmazonCDNFileUploaderImpl.getInstance()
                amazonFileUploaderInstance.authenticate()
                amazonFileUploaderInstance.deleteFile(containerName, ufileInstance.fullName)
                amazonFileUploaderInstance.close()
            } else {
                rackspaceCDNFileUploaderService.deleteFile(containerName, ufileInstance.fullName)
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
        String msg = messageSource.getMessage("fileupload.download.nofile", [idUfile] as Object[], locale)
        throw new FileNotFoundException(msg)
    }

    /**
     * Access the file held by the UFile, incrementing the viewed number, and returning appropriate message if file does not exist.
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

        String msg = messageSource.getMessage("fileupload.download.filenotfound", [ufileInstance.name] as Object[], locale)
        throw new IOException(msg)
    }

    /**
     * Method to create a duplicate of an existing UFile
     * @param group
     * @param ufileInstance
     * @param name
     * @param locale
     * @throws FileUploaderServiceException
     * @throws IOException
     */
    @Transactional
    UFile cloneFile(String group, UFile ufileInstance, String name = "", Locale locale = null) throws FileUploaderServiceException, IOException {
        log.info "Cloning ufile [${ufileInstance?.id}][${ufileInstance?.name}]"
        if (!ufileInstance) {
            log.warn "Invalid/null ufileInstance received."
            return null
        }

        String tempFile = getNewTemporaryDirectoryPath() + (name ?: ufileInstance.getFullName())

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
                fos.write(new URL(sourceFilePath).getBytes())
            } finally {
                fos.close()
            }
        } else {
            File sourceFile = new File(sourceFilePath)
            FileChannel source = null
            FileChannel destination = null

            try {
                source = new FileInputStream(sourceFile).getChannel()
                destination = new FileOutputStream(destFile).getChannel()
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
            log.error "No UFile instance found to resolve path"
            return ""
        }

        if (ufileInstance.type == UFileType.LOCAL) {
            return "/fileUploader/show/$ufileInstance.id"
        } else if (ufileInstance.type == UFileType.CDN_PUBLIC) {
            return ufileInstance.path
        }
    }

    List<Long> moveFileToCloud(List<UFile> ufileInstanceList, String containerName) {
        List<Long> failedUFileIdList = []
        List<BlobDetail> blobDetailList = []

        ufileInstanceList.each {
            String fullName = it.fullName.trim().replaceAll(" ", "_").replaceAll("-", "_")
            String newFileName = "${it.fileGroup}-${System.currentTimeMillis()}-${fullName}"
            blobDetailList << new BlobDetail(newFileName, new File(it.path), it.id)
            Thread.sleep(2)
        }
        containerName = UFile.containerName(containerName)

        rackspaceCDNFileUploaderService.uploadFilesToCloud(containerName, blobDetailList)
        String baseURL = rackspaceCDNFileUploaderService.cdnEnableContainer(containerName)

        blobDetailList.each {
            UFile uploadUFileInstance = ufileInstanceList.find { ufileInstance ->
                it.ufileId == ufileInstance.id
            }
            if (uploadUFileInstance) {
                if (it.eTag) {
                    uploadUFileInstance.name = it.remoteBlobName
                    uploadUFileInstance.path = baseURL + "/" + it.remoteBlobName
                    uploadUFileInstance.type = UFileType.CDN_PUBLIC
                    uploadUFileInstance.save(flush: true)
                } else {
                    failedUFileIdList << it.ufileId
                }
            } else {
                log.error "Missing blobInstance. Never reach condition occured."
            }
        }
        return failedUFileIdList
    }

    void renewTemporaryURL() {
        AmazonCDNFileUploaderImpl amazonFileUploaderInstance = AmazonCDNFileUploaderImpl.getInstance()
        amazonFileUploaderInstance.authenticate()

        UFile.withCriteria {
            eq("type", UFileType.CDN_PUBLIC)
            eq("provider", CDNProvider.AMAZON)
            or {
                lt("expiresOn", new Date())
                between("expiresOn", new Date(), new Date() + 1) // Getting all CDN UFiles which are about to expire within one day.
            }
        }.each { UFile ufileInstance ->
            log.debug "Renewing URL for $ufileInstance"

            String containerName = ufileInstance.container
            String fileFullName = ufileInstance.fullName
            long expirationPeriod = getExpirationPeriod(ufileInstance.fileGroup)

            ufileInstance.path = amazonFileUploaderInstance.getTemporaryURL(containerName, fileFullName, expirationPeriod)
            ufileInstance.expiresOn = new Date(new Date().time + expirationPeriod * 1000)
            ufileInstance.save(flush: true)
            if (ufileInstance.hasErrors()) {
                log.warn "Error saving new URL for $ufileInstance"
            }

            log.info "New URL for $ufileInstance [$ufileInstance.path] [$ufileInstance.expiresOn]"
        }

        amazonFileUploaderInstance.close()
    }

    long getExpirationPeriod(String fileGroup) {
        return Holders.getFlatConfig()["fileuploader.${fileGroup}.expirationPeriod"] ?: (Time.DAY * 30)      // Default to 30 Days
    }

    /**
     * Retrieves content of the given url and stores it in the temporary directory.
     * 
     * @param url The URL from which file to be retrieved
     * @param filename Name of the file
     */
    @Transactional
    File getFileFromURL(String url, String filename) {
        String path = getNewTemporaryDirectoryPath()

        File file = new File(path + filename)
        FileOutputStream fos = new FileOutputStream(file)
        fos.write(new URL(url).getBytes())
        fos.close()

        // Delete the temporary file when JVM exited since the base file is not required after upload
        file.deleteOnExit()

        return file
    }

    /**
     *  Time difference (in seconds) from provided date to current date. Since the "Cache-Control" header accepts
     *  max-age in delta seconds, so we need to convert expiratation date to seconds from current date.
     * 
     * @param expirationDate Date from which max-age needs to be retrived
     * @returns long Time difference (in seconds) from provided date to current date
     * @see http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.9
     */
    long getMaxAge(Date expirationDate) {
        TimeDuration timeDifference = TimeCategory.minus(expirationDate, new Date())
        return timeDifference.days * 86400 +  timeDifference.hours * 3600 + timeDifference.minutes * 60 + timeDifference.seconds
    }
    
    void updateUFilesCacheHeader() {
        ConfigObject config = Holders.getConfig().fileuploader

        List uFileList = UFile.list()
        uFileList.each { UFile uFileInstance ->

            String containerName = uFileInstance.getContainer()
            CDNProvider cdnProvider = uFileInstance.provider
            if (cdnProvider == CDNProvider.AMAZON
                    && (uFileInstance.expiresOn == null || uFileInstance.expiresOn > new Date())) {

                println uFileInstance.name + "\n" + uFileInstance.path
                AmazonCDNFileUploaderImpl amazonFileUploaderInstance = AmazonCDNFileUploaderImpl.getInstance()
                amazonFileUploaderInstance.authenticate()

                long maxAge = getMaxAge(uFileInstance.expiresOn)
                amazonFileUploaderInstance.updateS3ObjectMetaData(containerName, uFileInstance.getFullName(), maxAge)
                amazonFileUploaderInstance.close()
            }
        }
    }
}