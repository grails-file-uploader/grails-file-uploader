/*
 * Copyright (c) 2011-Present, CauseCode Technologies Pvt Ltd, India.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or
 * without modification, are not permitted.
 */
package com.causecode.fileuploader.util

import grails.util.Holders
import org.springframework.web.multipart.MultipartFile

/**
 * A utility class to hold common methods and default configurations required for this plugin.
 *
 * TODO Find a workaround for using plugins default configuration.
 * @author Ankit Agrawal
 * @since 3.0.1
 */
@SuppressWarnings(['JavaIoPackageAccess'])
class FileUploaderUtils {
    static String baseTemporaryDirectoryPath

    static {
        baseTemporaryDirectoryPath = Holders.flatConfig['grails.tempDirectory'] ?: './temp'

        if (!baseTemporaryDirectoryPath.endsWith('/')) {
            baseTemporaryDirectoryPath += '/'
        }

        // Make sure directory exists
        File tempDirectory = new File(baseTemporaryDirectoryPath)
        tempDirectory.mkdirs()
    }

    /**
     * This method creates a directory at the temporary location and returns the path of it.
     * @return {@link String} path of temporary directory
     */
    @SuppressWarnings('JavaIoPackageAccess')
    static String getNewTemporaryDirectoryPath() {
        String tempDirectoryPath = baseTemporaryDirectoryPath + UUID.randomUUID().toString() + '/'
        File tempDirectory = new File(tempDirectoryPath)
        tempDirectory.mkdirs()

        // Delete the temporary directory when JVM exited
        tempDirectory.deleteOnExit()

        return tempDirectoryPath
    }

    @SuppressWarnings('JavaIoPackageAccess')
    static File getTempFilePathForMultipartFile(String fileName, String fileExtension) {
        return new File(newTemporaryDirectoryPath + "${fileName}.${fileExtension}")
    }

    /**
     * Method is used to move file from temp directory to another.
     * @params fileInstance , path
     *
     */
    @SuppressWarnings(['Instanceof', 'JavaIoPackageAccess'])
    static void moveFile(def file, String path) {
        if (file instanceof File) {
            file.renameTo(new File(path))
        } else {
            if (file instanceof MultipartFile) {
                file.transferTo(new File(path))
            }
        }
    }
}
