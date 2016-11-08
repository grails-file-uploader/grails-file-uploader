/*
 * Copyright (c) 2016, CauseCode Technologies Pvt Ltd, India.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or
 * without modification, are not permitted.
 */
package com.lucastex.grails.fileuploader.cdn.google

import com.google.cloud.storage.Blob
import com.google.cloud.storage.BlobId
import com.google.cloud.storage.BlobInfo
import com.google.cloud.storage.BucketInfo
import com.google.cloud.storage.Storage
import com.google.cloud.storage.StorageException
import com.lucastex.grails.fileuploader.GoogleStorageException
import com.lucastex.grails.fileuploader.UploadFailureException
import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import spock.lang.Specification

@TestMixin(GrailsUnitTestMixin)
class GoogleCDNFileUploaderImplSpec extends Specification {

    GoogleCDNFileUploaderImpl googleCDNFileUploaderImpl

    def setup() {
        GoogleCDNFileUploaderImpl.metaClass.getBlob = { String containerName, String fileName ->
            return new Blob(googleCDNFileUploaderImpl.gStorage,
                    new BlobInfo.BuilderImpl(new BlobId("dummyContainer", "testFile", 2l)))
        }

        Storage.metaClass.create = { BucketInfo var1, Storage.BucketTargetOption... var2 ->
            throw new StorageException(1, "Test exception")
        }

        googleCDNFileUploaderImpl = new GoogleCDNFileUploaderImpl()
    }

    void "test Google Cloud Storage for delete failure"() {
        given: "mocked 'delete' method for Blob class"
        Blob.metaClass.delete = { Blob.BlobSourceOption... options ->
            return false
        }

        when: "deleteFile() method is called"
        googleCDNFileUploaderImpl.deleteFile("dummyContainer", "testFile")

        then: "it should throw GoogleStorageException exception"
        GoogleStorageException e = thrown()
        e.message == "Could not delete file testFile from container dummyContainer"
    }

    void "test Google Cloud Storage for upload failure"() {
        given: "A file instance and mocked 'of' method of class BlobId"
        File file = new File('test.txt')
        file.createNewFile()
        file << 'This is a test document.'

        and: "Mocked methods"
        BlobId.metaClass.of = { String containerName, String fileName ->
            return new BlobId("dummyContainer", "test", 2l)
        }
        Storage.metaClass.create = { BlobInfo blobInfo, byte[] content, Storage.BlobTargetOption... options->
            throw new StorageException(1, "Test exception")
        }

        when: "uploadFile() method  is called"
        googleCDNFileUploaderImpl.uploadFile("dummyContainer", file, "test", false, 3600l)

        then: "it should throw UploadFailureException"
        UploadFailureException e = thrown()
        e.message == "Could not upload file test to container dummyContainer"

        cleanup:
        file.delete()
    }

    void "test Google Cloud Storage for create Container failure"() {
        when: "createContainer() method is called"
        googleCDNFileUploaderImpl.createContainer("dummyContainer")

        then: "it should throw GoogleStorageException exception"
        GoogleStorageException e = thrown()
        e.message == "Could not create container."
    }

    /* This test case was written to check if a file was actually uploaded to cloud. To enable this test case-
    * 1. comment out setup block
    * 2. Set GOOGLE_APPLICATION_CREDENTIALS environment variable.
    * 3. Run test-app via command line
    */
//    void "test Google Cloud Storage for successfully uploading a file"() {
//        given: "A file instance"
//        File file = new File('test.txt')
//        file.createNewFile()
//        file << "This is a test document"
//
//        when: "uploadFile method is called"
//        def result = googleCDNFileUploaderImpl.uploadFile("causecode-test", file, "test", false, 3600l)
//
//        then: "No exception is thrown and file gets uploaded, method returns true"
//        notThrown(UploadFailureException)
//        result
//    }
}