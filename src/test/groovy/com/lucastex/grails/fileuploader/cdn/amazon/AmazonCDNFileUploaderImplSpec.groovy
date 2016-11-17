/*
 * Copyright (c) 2016, CauseCode Technologies Pvt Ltd, India.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or
 * without modification, are not permitted.
 */
package com.lucastex.grails.fileuploader.cdn.amazon

import com.lucastex.grails.fileuploader.BaseTestSetup
import com.lucastex.grails.fileuploader.UploadFailureException
import com.lucastex.grails.fileuploader.cdn.CDNFileUploader
import grails.test.mixin.Mock
import org.jclouds.aws.s3.AWSS3Client
import org.jclouds.blobstore.KeyNotFoundException
import org.jclouds.s3.domain.AccessControlList
import org.jclouds.s3.domain.internal.S3ObjectImpl
import spock.lang.Specification
import spock.util.mop.ConfineMetaClassChanges

@ConfineMetaClassChanges([AccessControlList])
class AmazonCDNFileUploaderImplSpec extends Specification implements BaseTestSetup {

    AmazonCDNFileUploaderImpl amazonCDNFileUploaderImpl

    def setup() {
        amazonCDNFileUploaderImpl = new AmazonCDNFileUploaderImpl()
    }

//    void "test authenticate method for successful response"() {
//        when: "autenticate method is called"
//        boolean response = amazonCDNFileUploaderImpl.authenticate()
//
//        then: "Method returns true"
//        response
//    }

    void "test Amazon Cloud Storage for upload failure"() {
        given: "A file instance"
        File file = getFileInstance()

        and: "Mocked method"
        AWSS3Client clientInstance = Mock(AWSS3Client)
        clientInstance.putObject(_, _, _) >> { throw new Exception('test exception') }
        amazonCDNFileUploaderImpl.client = clientInstance

        when: "uploadFile method is called"
        amazonCDNFileUploaderImpl.uploadFile("dummyContainer", file, "test", false, 3600l)

        then: "Method should throw UploadFailureException"
        UploadFailureException e = thrown()
        e.message == "Could not upload file test to container dummyContainer"

        cleanup:
        file.delete()
    }

    void "test Amazon Cloud Storage for upload success"() {
        given: "A file instance"
        File file = getFileInstance()

        and: "Mocked method"
        AWSS3Client clientInstance = Mock(AWSS3Client)
        clientInstance.putObject(_, _, _) >> { return 'test values' }
        amazonCDNFileUploaderImpl.client = clientInstance

        when: "uploadFile method is called"
        boolean result = amazonCDNFileUploaderImpl.uploadFile("dummyContainer", file, "test", false, 3600l)

        then: "Method should return true"
        result

        cleanup:
        file.delete()
    }

    void "test makeFilePublic method for failure case"() {
        given: "Mocked methods"
        AccessControlList.metaClass.addPermission = { URI groupGranteeURI, String permission ->
            return
        }

        AWSS3Client clientInstance = Mock(AWSS3Client)
        clientInstance.getObject(_, _, _) >> { return new S3ObjectImpl() }
        amazonCDNFileUploaderImpl.client = clientInstance

        when: "makeFilePublic method is called"
        boolean result = amazonCDNFileUploaderImpl.makeFilePublic('dummy', 'test')

        then: "Method returns false as response"
        !result
    }

    void "test updatePreviousFileMetaData for update failure"() {
        given: "Mocked method"
        AWSS3Client clientInstance = Mock(AWSS3Client)
        clientInstance.copyObject(_, _, _, _, _) >> { throw new KeyNotFoundException() }
        amazonCDNFileUploaderImpl.client = clientInstance

        when: "updatePreviousFileMetaData method is called"
        amazonCDNFileUploaderImpl.updatePreviousFileMetaData('dummy', 'test', true, 3600L)

        then: "No Exception is thrown"
        noExceptionThrown()
    }

    // TODO ------ find a way to handle null pointer exceptions
//    void "test getTemporaryURL method to return temporary url"() {
//        given: ""
//
//        when: "getTemporaryURL method is called"
//        String response = amazonCDNFileUploaderImpl.getTemporaryURL('dummy', 'test', 3600L)
//
//        then: "Method returns url"
//        println response
//    }

    void "test various methods for successFul execution"() {
        given: "Mocked methods"
        AWSS3Client clientInstance = Mock(AWSS3Client)
        clientInstance.bucketExists(_) >> { return true }
        amazonCDNFileUploaderImpl.client = clientInstance

        expect:
        amazonCDNFileUploaderImpl.containerExists('test') == true
    }
}