/*
 * Copyright (c) 2016, CauseCode Technologies Pvt Ltd, India.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or
 * without modification, are not permitted.
 */
package com.causecode.fileuploader

import com.causecode.fileuploader.cdn.amazon.AmazonCDNFileUploaderImpl
import com.causecode.fileuploader.cdn.google.GoogleCDNFileUploaderImpl
import com.causecode.fileuploader.cdn.google.GoogleCredentials
import org.apache.commons.fileupload.disk.DiskFileItem
import spock.lang.Specification

/**
 * This class contains base setup and utility methods to mock service method calls, to be used in
 * FileUploaderServiceSpec class.
 */
class BaseFileUploaderServiceSpecSetup extends Specification implements BaseTestSetup {

    void setup() {
        GoogleCredentials.metaClass.getStorage = { ->
            return
        }

        AmazonCDNFileUploaderImpl.metaClass.close = { ->
            return true
        }

        GoogleCDNFileUploaderImpl.metaClass.close = { ->
            return true
        }

        Closure getTemporaryURL = { String containerName, String fileName, long expiration ->
            return 'http://fixedURL.com'
        }

        Closure uploadFile = { String containerName, File file, String fileName, boolean makePublic, long maxAge ->
            return true
        }

        AmazonCDNFileUploaderImpl.metaClass.getTemporaryURL = getTemporaryURL
        GoogleCDNFileUploaderImpl.metaClass.getTemporaryURL = getTemporaryURL

        AmazonCDNFileUploaderImpl.metaClass.uploadFile = uploadFile
        GoogleCDNFileUploaderImpl.metaClass.uploadFile = uploadFile
    }

    DiskFileItem getFileItem(File fileInstance) {
        DiskFileItem fileItem = new DiskFileItem('file', 'text/plain', false, fileInstance.name,
                (int) fileInstance.length() , fileInstance.parentFile)
        fileItem.outputStream
        return fileItem
    }

    void mockGetFileNameAndExtensions() {
        FileGroup.metaClass.getFileNameAndExtensions = { def file, String customFileName ->
            return [fileName: 'test.txt', fileExtension: 'txt', customFileName: 'unit-test', empty: false,
                    fileSize: 38L]
        }
    }

    void mockExistMethod(boolean boolResult) {
        File.metaClass.exists = {
            return boolResult
        }
    }

    boolean mockAuthenticateMethod() {
        AmazonCDNFileUploaderImpl.metaClass.authenticate = {
            return true
        }
    }

    void mockGetPermanentURL() {
        Closure getPermanentURL = { String containerName, String fileName ->
            return 'http://fixedURL.com'
        }

        AmazonCDNFileUploaderImpl.metaClass.getPermanentURL = getPermanentURL
        GoogleCDNFileUploaderImpl.metaClass.getPermanentURL = getPermanentURL
    }
}
