/*
 * Copyright (c) 2016, CauseCode Technologies Pvt Ltd, India.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or
 * without modification, are not permitted.
 */
package com.causecode.fileuploader.cdn

import com.causecode.fileuploader.BaseTestSetup
import com.causecode.fileuploader.UFile
import grails.test.mixin.Mock
import spock.lang.Specification

/**
 * This class contains unit test cases for BlobDetail class
 */
@Mock([UFile])
class BlobDetailSpec extends Specification implements BaseTestSetup {

    void "test BlobDetails class methods"() {
        given: 'An instance of BlobDetails'
        UFile uFileInstance = getUFileInstance(1)
        BlobDetail blobDetailInstance = new BlobDetail('test', null, uFileInstance, 'tag')

        when: 'getRemoteBlobName method is called'
        def result = blobDetailInstance.remoteBlobName

        then: 'The method should return remoteBlobName'
        result == 'test'

        when: 'getLocalFile method is called'
        result = blobDetailInstance.localFile

        then: 'Method returns null in response'
        result == null

        when: 'getETag method is called'
        result = blobDetailInstance.ETag

        then: 'Method should return eTag value'
        result == 'tag'

        when: 'isUploaded method is called'
        result = blobDetailInstance.isUploaded()

        then: 'Method should return true'
        result

        when: 'toString method is called'
        result = blobDetailInstance.toString()

        then: 'Method response should match the expected result'
        result == "{$blobDetailInstance.remoteBlobName}{$blobDetailInstance.localFile}{$blobDetailInstance.ufile.id}"

        when: 'isUploaded method is called and eTag is null'
        blobDetailInstance = new BlobDetail('test', null, uFileInstance)
        blobDetailInstance.eTag = null
        result = blobDetailInstance.isUploaded()

        then: 'Method should return false'
        !result
    }
}
