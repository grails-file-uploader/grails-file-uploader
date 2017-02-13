/*
 * Copyright (c) 2016, CauseCode Technologies Pvt Ltd, India.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or
 * without modification, are not permitted.
 */
package com.causecode.fileuploader.cdn.amazon

import com.causecode.fileuploader.BaseTestSetup
import com.causecode.fileuploader.UploadFailureException
import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import org.jclouds.aws.s3.AWSS3Client
import org.jclouds.blobstore.KeyNotFoundException
import org.jclouds.http.HttpResponseException
import org.jclouds.s3.domain.internal.S3ObjectImpl
import spock.lang.Specification

/**
 * This class contains unit test cases for AmazonCDNFileUploaderImpl class.
 */
@TestMixin(GrailsUnitTestMixin)
class AmazonCDNFileUploaderImplSpec extends Specification implements BaseTestSetup {

    AmazonCDNFileUploaderImpl amazonCDNFileUploaderImpl

    def setup() {
        amazonCDNFileUploaderImpl = new AmazonCDNFileUploaderImpl()
    }

    void "test Amazon Cloud Storage for upload failure"() {
        given: 'A file instance'
        File fileInstance = getFileInstance('./temp/test.txt')

        and: 'Mocked method'
        AWSS3Client clientInstance = Mock(AWSS3Client)
        clientInstance.putObject(_, _, _) >> { throw new HttpResponseException('Test exception', null, null) }
        amazonCDNFileUploaderImpl.client = clientInstance

        when: 'uploadFile method is called'
        amazonCDNFileUploaderImpl.uploadFile('dummyContainer', fileInstance, 'test', false, 3600L)

        then: 'Method should throw UploadFailureException'
        UploadFailureException e = thrown()
        e.message == 'Could not upload file test to container dummyContainer'

        cleanup:
        fileInstance.delete()
    }

    void "test Amazon Cloud Storage for upload success"() {
        given: 'A file instance'
        File fileInstance = getFileInstance('./temp/test.txt')

        and: 'Mocked method'
        AWSS3Client clientInstance = Mock(AWSS3Client)
        clientInstance.putObject(_, _, _) >> { return 'test values' }
        amazonCDNFileUploaderImpl.client = clientInstance

        when: 'uploadFile method is called'
        boolean result = amazonCDNFileUploaderImpl.uploadFile('dummyContainer', fileInstance, 'test', false, 3600L)

        then: 'Method should return true'
        result

        cleanup:
        fileInstance.delete()
    }

    void "test makeFilePublic method for failure case"() {
        given: 'Mocked methods'
        AWSS3Client clientInstance = Mock(AWSS3Client)
        clientInstance.getObject(_, _, _) >> { return new S3ObjectImpl() }
        amazonCDNFileUploaderImpl.client = clientInstance

        when: 'makeFilePublic method is called'
        boolean result = amazonCDNFileUploaderImpl.makeFilePublic('dummy', 'test')

        then: 'Method returns false as response'
        !result
    }

    void "test updatePreviousFileMetaData for update failure"() {
        given: 'Mocked method'
        AWSS3Client clientInstance = Mock(AWSS3Client)
        clientInstance.copyObject(_, _, _, _, _) >> { throw new KeyNotFoundException() }
        amazonCDNFileUploaderImpl.client = clientInstance

        when: 'updatePreviousFileMetaData method is called'
        amazonCDNFileUploaderImpl.updatePreviousFileMetaData('dummy', 'test', true, 3600L)

        then: 'No Exception is thrown'
        noExceptionThrown()
    }

    void "test containerExists method for successFul execution"() {
        given: 'Mocked methods'
        AWSS3Client clientInstance = Mock(AWSS3Client)
        clientInstance.bucketExists(_) >> { return true }
        amazonCDNFileUploaderImpl.client = clientInstance

        expect:
        amazonCDNFileUploaderImpl.containerExists('test') == true
    }

    void "test getTemporaryUrl method to get temporary url of a file"() {
        expect:
        amazonCDNFileUploaderImpl.getTemporaryURL('testGoogle', 'test', 1L) != null
    }
}
