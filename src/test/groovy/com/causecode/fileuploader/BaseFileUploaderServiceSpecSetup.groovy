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
import com.causecode.fileuploader.provider.ProviderService
import spock.lang.Specification

/**
 * This class contains base setup and utility methods to mock service method calls, to be used in
 * FileUploaderServiceSpec class.
 */
class BaseFileUploaderServiceSpecSetup extends Specification implements BaseTestSetup {

    AmazonCDNFileUploaderImpl amazonCDNFileUploaderInstance

    GoogleCDNFileUploaderImpl googleCDNFileUploaderImplMock
    AmazonCDNFileUploaderImpl amazonCDNFileUploaderImplMock
    GoogleCredentials googleCredentialsMock
    FileGroup fileGroupMock

    void setup() {
        amazonCDNFileUploaderInstance = new AmazonCDNFileUploaderImpl()
        googleCDNFileUploaderImplMock = GroovyMock(GoogleCDNFileUploaderImpl, global: true)
        amazonCDNFileUploaderImplMock = GroovyMock(AmazonCDNFileUploaderImpl, global: true)
        googleCredentialsMock = GroovyMock(GoogleCredentials, global: true)
        fileGroupMock = GroovyMock(FileGroup, global: true)

        googleCredentialsMock.storage >> {
            return
        }

        amazonCDNFileUploaderImplMock.close() >> {
            return
        }

        googleCredentialsMock.close() >> {
            return
        }
    }

    void mockGetFileNameAndExtensions() {
        fileGroupMock.getFileNameAndExtensions(_, _) >> {
            return [fileName: 'test.txt', fileExtension: 'txt', customFileName: 'unit-test', empty: false,
                    fileSize: 38L]
        }
    }

    void mockExistMethod(boolean boolResult) {
        File.metaClass.exists = {
            return boolResult
        }
    }

    void mockFileDeleteMethod(boolean boolResult) {
        File.metaClass.delete = {
            return boolResult
        }
    }

    boolean mockAuthenticateMethod() {
        new AmazonCDNFileUploaderImpl() >> amazonCDNFileUploaderImplMock
        amazonCDNFileUploaderImplMock.authenticate() >> {
            return true
        }
    }

    void mockGetPermanentURL() {
        amazonCDNFileUploaderImplMock.getPermanentURL(_, _) >> 'http://fixedURL.com'
        googleCDNFileUploaderImplMock.getPermanentURL(_, _) >> 'http://fixedURL.com'
    }

    void mockGetTemporaryURL() {
        amazonCDNFileUploaderImplMock.getTemporaryURL(_, _, _) >> 'http://fixedURL.com'
        googleCDNFileUploaderImplMock.getTemporaryURL(_, _, _) >> 'http://fixedURL.com'
        amazonCDNFileUploaderImplMock.getPermanentURL(_, _) >> 'http://fixedURL.com'
        googleCDNFileUploaderImplMock.getPermanentURL(_, _) >> 'http://fixedURL.com'
    }

    void mockUploadFileMethod(boolean value) {
        amazonCDNFileUploaderImplMock.uploadFile(_, _, _, _, _) >> value
        googleCDNFileUploaderImplMock.uploadFile(_, _, _, _, _) >> value
    }

    void mockGetProviderInstance(String provider) {
        service.providerService.getProviderInstance(_) >> { String providerName ->
            provider == 'google' ? googleCDNFileUploaderImplMock : amazonCDNFileUploaderImplMock
        }
    }

    void mockFileGroupConstructor(String storageTypes) {
        new FileGroup(_) >> { String group ->
            fileGroupMock.groupName = group
            fileGroupMock.containerName >> 'test-container'
            fileGroupMock.groupConfig >> [storageTypes: storageTypes]

            return fileGroupMock
        }
    }
}
