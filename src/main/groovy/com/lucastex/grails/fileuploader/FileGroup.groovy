/*
 * Copyright (c) 2016, CauseCode Technologies Pvt Ltd, India.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or
 * without modification, are not permitted.
 */
package com.lucastex.grails.fileuploader

import grails.util.Holders
import groovy.util.logging.Slf4j
import org.springframework.context.support.AbstractMessageSource
import org.springframework.web.multipart.commons.CommonsMultipartFile

/**
 * A utility class which performs operations on properties of a file.
 *
 * @author Ankit Agrawal
 * @since 3.0.1
 */
@Slf4j
@SuppressWarnings(['Instanceof', 'JavaIoPackageAccess'])
class FileGroup {
    ConfigObject groupConfig, config
    AbstractMessageSource messageSource
    private static final String HYPHEN = '-'
    private static final String SLASH = '/'

    FileGroup(String group) {
        config = Holders.config.fileuploader.groups
        groupConfig = config[group]
    }

    /**
     * This method is used to get File name, extension and other required properties.
     * @params file, customFileName
     * @return Map containing fileName, fileExtension, customFileName, fileSize and empty.
     *
     */
    Map getFileNameAndExtensions(def file, String customFileName) {
        String contentType, receivedFileName, fileName, fileExtension
        long fileSize
        boolean empty = true

        if (file instanceof File) {
            contentType = ''
            empty = !file.exists()
            receivedFileName = file.name
            fileSize = file.size()
        } else {
            if (file instanceof CommonsMultipartFile) {    // Means instance is of Spring's CommonsMultipartFile.
                def uploaderFile = file
                contentType = uploaderFile?.contentType
                empty = uploaderFile?.isEmpty()
                receivedFileName = uploaderFile?.originalFilename
                fileSize = uploaderFile?.size
            }
        }
        log.info "Received ${empty ? 'empty ' : ''} file [$receivedFileName] of size [$fileSize] & content type " +
                "[$contentType]."

        int extensionAt = receivedFileName.lastIndexOf('.')
        if (extensionAt > -1) {
            fileName = customFileName ?: receivedFileName[0..(extensionAt - 1)]
            fileExtension = receivedFileName[(extensionAt + 1)..-1].toLowerCase().trim()
        } else {
            fileName = customFileName ?: fileName
        }
        /**
         * Convert all white space to underscore and hyphens to underscore to differentiate
         * different data on filename created below.
         */
        fileName.trim().replaceAll(" ", "_").replaceAll("-", "_")

        return [fileName: fileName, fileExtension: fileExtension, customFileName: customFileName, empty: empty,
                fileSize: fileSize]
    }

    /**
     * This method is used to check if the file under save process has the correct extension.
     * If not this method would throw StorageConfigurationException.
     * @throws StorageConfigurationException
     *
     */
    void allowedExtensions(Map fileGroupMap, Locale locale, String group) {
        if (this.groupConfig.isEmpty()) {
            throw new StorageConfigurationException("No config defined for group [$group]. " +
                    'Please define one in your Config file.')
        }

        if ((this.groupConfig.allowedExtensions[0] != ('*')) && !this.groupConfig.allowedExtensions.contains
                (fileGroupMap.fileExtension)) {
            String msg = messageSource.getMessage('fileupload.upload.unauthorizedExtension',
                    [fileGroupMap.fileExtension, this.groupConfig.allowedExtensions] as Object[], locale)
            log.debug msg
            throw new StorageConfigurationException(msg)
        }
    }

    /**
     * This method is used to validate the size of File being saved.
     * @throws StorageConfigurationException
     *
     */
    def validateFileSize(Map fileGroupMap, Locale locale) {
        /**
         * If maxSize config exists
         */
        if (this.groupConfig.maxSize) {
            def maxSizeInKb = ((int) (this.groupConfig.maxSize)) / 1024
            if (fileGroupMap.fileSize > this.groupConfig.maxSize) { //if filesize is bigger than allowed
                log.debug "FileUploader plugin received a file bigger than allowed. Max file size is ${maxSizeInKb} kb"
                def msg = messageSource.getMessage('fileupload.upload.fileBiggerThanAllowed',
                        [maxSizeInKb] as Object[], locale)
                throw new StorageConfigurationException(msg)
            }
        }
    }

    /**
     * This method is used to get local system file path. It is used when storageType is not CDN.
     * @params storageTypes, fileGroupMap, currentTimeMillis
     * @return path where file would be saved in local file system.
     *
     */
    String getLocalSystemPath(String storageTypes, Map fileGroupMap, long currentTimeMillis) {
        // Base path to save file
        String localPath = this.groupConfig.path
        if (!localPath.endsWith(SLASH)) {
            localPath = localPath + SLASH
        }

        if (storageTypes?.contains('monthSubdirs')) {  //subdirectories by month and year
            Calendar cal = Calendar.instance
            localPath = localPath + cal[Calendar.YEAR].toString() + cal[Calendar.MONTH].toString() + SLASH
        } else {  //subdirectories by millisecond
            localPath = localPath + currentTimeMillis + SLASH
        }

        // Make sure the directory exists
        if (!new File(localPath).exists()) {
            if (!new File(localPath).mkdirs()) {
                log.error "FileUploader plugin couldn't create directories: [${path}]"
            }
        }

        // If using the uuid storage type
        if (storageTypes?.contains('uuid')) {
            localPath = localPath + UUID.randomUUID().toString()
        } else {  //note:  this type of storage is a bit of a security / data loss risk.
            localPath = localPath + fileGroupMap.fileName + '.' + fileGroupMap.fileExtension
        }

        return localPath
    }

    /**
     * This method is used to modify fileName obtained from fileGroupMap.
     * @params userInstance, fileGroupMap, group, currentTimeMillis
     *
     */
    void scopeFileName(Object userInstance, Map fileGroupMap, String group, Long currentTimeMillis) {
        String containerNameString = getContainerName()

        if (!containerNameString) {
            throw new StorageConfigurationException('Container name not defined in the Config. Please define one.')
        }

        StringBuilder fileNameBuilder = new StringBuilder(group)
                .append(HYPHEN)

        StringBuilder fileName = new StringBuilder(fileGroupMap.fileName)

        if (userInstance && userInstance.id) {
            fileName.append(userInstance.id.toString())
            fileName.append(HYPHEN)
        }

        fileNameBuilder.append(currentTimeMillis)
                .append(HYPHEN)
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
        fileGroupMap.fileName = fileNameBuilder.toString()
    }

    // Method which fetches containerName from application.groovy file and returns it.
    String getContainerName() {
        return UFile.containerName(this.groupConfig.container ?: this.config.container)
    }

    // Method that fetched CDNProvider from the config and returns it.
    CDNProvider getCdnProvider() {
        return this.groupConfig.provider ?: this.config.provider
    }
}
