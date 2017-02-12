/*
 * Copyright (c) 2016, CauseCode Technologies Pvt Ltd, India.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or
 * without modification, are not permitted.
 */
package com.causecode.fileuploader

import grails.util.Holders
import groovy.util.logging.Slf4j
import org.springframework.context.MessageSource
import org.springframework.web.multipart.MultipartFile

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
    String groupName
    MessageSource messageSource

    FileGroup(String group) {
        groupName = group
        config = Holders.config.fileuploader.groups
        groupConfig = config[group]
    }

    /**
     * This method is used to modify File name, get extension and other required properties.
     * @params file,
     * @params customFileName - Custom file name without extension.
     * @return Map containing fileExtension,, updated fileName, customFileName, fileSize and empty.
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
            if (file instanceof MultipartFile) {    // Means instance is of Spring's MultipartFile.
                def uploaderFile = file
                contentType = uploaderFile?.contentType
                empty = uploaderFile?.isEmpty()
                receivedFileName = uploaderFile?.originalFilename
                fileSize = uploaderFile?.size
            }
        }
        log.info "Received ${empty ? 'empty ' : ''} file [$receivedFileName] of size [$fileSize] & content " +
                "type [$contentType]."

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
        fileName.trim().replaceAll(' ', '_').replaceAll('-', '_')

        return [fileName: fileName, fileExtension: fileExtension, customFileName: customFileName,
                empty: empty, fileSize: fileSize]
    }

    /**
     * This method is used to check if the file to be saved has the correct extension.
     * If not the method would throw StorageConfigurationException.
     * @throws StorageConfigurationException
     *
     */
    void allowedExtensions(Map fileDataMap, Locale locale, String group) {
        if (this.groupConfig.isEmpty()) {
            throw new StorageConfigurationException("No config defined for group [$group]. " +
                    'Please define one in your Config file.')
        }

        if ((this.groupConfig.allowedExtensions[0] != ('*')) && !this.groupConfig.allowedExtensions.contains
                (fileDataMap.fileExtension)) {
            String msg = messageSource.getMessage('fileupload.upload.unauthorizedExtension',
                    [fileDataMap.fileExtension, this.groupConfig.allowedExtensions] as Object[], locale)
            log.debug msg
            throw new StorageConfigurationException(msg)
        }
    }

    /**
     * This method is used to validate the size of File being saved.
     * @throws StorageConfigurationException
     *
     */
    def validateFileSize(Map fileDataMap, Locale locale) {
        /**
         * If maxSize config exists
         */
        if (this.groupConfig.maxSize) {
            def maxSizeInKb = ((int) (this.groupConfig.maxSize)) / 1024
            if (fileDataMap.fileSize > this.groupConfig.maxSize) { //if filesize is bigger than allowed
                log.debug "FileUploader plugin received a file bigger than allowed. Max file size is ${maxSizeInKb} kb"
                def msg = messageSource.getMessage('fileupload.upload.fileBiggerThanAllowed',
                        [maxSizeInKb] as Object[], locale)
                throw new StorageConfigurationException(msg)
            }
        }
    }

    /**
     * This method is used to get local system file path. It is used when storageType is not CDN.
     * @params storageTypes, fileDataMap, currentTimeMillis
     * @return path
     *
     */
    String getLocalSystemPath(String storageTypes, Map fileDataMap, long currentTimeMillis) {
        // Base path to save file
        String localPath = this.groupConfig.path
        if (!localPath.endsWith('/')) {
            localPath = localPath + '/'
        }

        if (storageTypes?.contains('monthSubdirs')) {  //subdirectories by month and year
            Calendar cal = Calendar.instance
            localPath = localPath + cal[Calendar.YEAR].toString() + cal[Calendar.MONTH].toString() + '/'
        } else {  //subdirectories by millisecond
            localPath = localPath + currentTimeMillis + '/'
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
            localPath = localPath + fileDataMap.fileName + '.' + fileDataMap.fileExtension
        }

        return localPath
    }

    /**
     * This method is used to modify fileName obtained from fileDataMap.
     * @params userInstance, fileDataMap, group, currentTimeMillis
     *
     * @throws StorageConfigurationException
     */
    void scopeFileName(Object userInstance, Map fileDataMap, String group, Long currentTimeMillis)
            throws StorageConfigurationException {
        String container = containerName

        if (!container) {
            throw new StorageConfigurationException('Container name not defined in the Config. Please define one.')
        }

        StringBuilder fileNameBuilder = new StringBuilder(group)
                .append('-')

        StringBuilder fileName = new StringBuilder(fileDataMap.fileName)

        if (userInstance && userInstance.id) {
            fileName.append(userInstance.id.toString())
            fileName.append('-')
        }

        fileNameBuilder.append(currentTimeMillis)
                .append('-')
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
        fileDataMap.fileName = fileNameBuilder.toString()
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
