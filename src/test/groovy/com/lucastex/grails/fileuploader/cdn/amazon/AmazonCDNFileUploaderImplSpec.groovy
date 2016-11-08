/*
 * Copyright (c) 2016, CauseCode Technologies Pvt Ltd, India.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or
 * without modification, are not permitted.
 */
package com.lucastex.grails.fileuploader.cdn.amazon

import com.lucastex.grails.fileuploader.UploadFailureException
import org.jclouds.aws.s3.AWSS3Client
import org.jclouds.blobstore.KeyNotFoundException
import org.jclouds.s3.domain.S3Object
import org.jclouds.s3.options.CopyObjectOptions
import org.jclouds.s3.options.PutObjectOptions
import spock.lang.Specification

class AmazonCDNFileUploaderImplSpec extends Specification {

    def setup() {
        AWSS3Client.metaClass.putObject = { String s, S3Object s3Object, PutObjectOptions... putObjectOptionses ->
            throw new Exception("Test exception")
        }
        AWSS3Client.metaClass.copyObject = { String s, String s1, String s2, String s3, CopyObjectOptions... copyObjectOptionses ->
            throw new KeyNotFoundException("dummyContainer", "dummyKey","Test exception")
        }
    }

    void "test Amazon Cloud Storage for upload failure"() {
        given: "A file instance"
        File file = new File('test.txt')
        file.createNewFile()
        file << 'This is a test document.'

        when: "uploadFile() method is called"
        AmazonCDNFileUploaderImpl amazonCDNFileUploaderImpl = new AmazonCDNFileUploaderImpl()
        amazonCDNFileUploaderImpl.uploadFile("dummyContainer", file, "test", false, 3600l)

        then: "it should throw UploadFailureException"
        UploadFailureException e = thrown()
        e.message == "Could not upload file test to container dummyContainer"

        cleanup:
        file.delete()
    }
}