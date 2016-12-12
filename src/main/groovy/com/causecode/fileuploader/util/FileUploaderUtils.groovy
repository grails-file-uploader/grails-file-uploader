/*
 * Copyright (c) 2011-Present, CauseCode Technologies Pvt Ltd, India.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or
 * without modification, are not permitted.
 */
package com.causecode.fileuploader.util

import grails.util.Holders

/**
 * A utility class to hold common methods and default configurations required for this plugin.
 *
 * TODO Find a workaround for using pluginns default configuration.
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
}
