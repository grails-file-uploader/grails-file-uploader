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
import spock.lang.Specification
import spock.util.mop.ConfineMetaClassChanges

@ConfineMetaClassChanges([Blob, GoogleCDNFileUploaderImpl, GoogleCredentials])
class GoogleCDNFileUploaderImplSpec extends Specification {

    GoogleCDNFileUploaderImpl googleCDNFileUploaderImpl

    def setup() {
        GoogleCredentials.metaClass.getStorage = {
            return Mock(Storage)
        }
        googleCDNFileUploaderImpl = new GoogleCDNFileUploaderImpl()
    }

    void mockGetBlobMethod() {
        GoogleCDNFileUploaderImpl.metaClass.getBlob = { String containerName, String fileName ->
            return new Blob(googleCDNFileUploaderImpl.gStorage,
                    new BlobInfo.BuilderImpl(new BlobId("dummyContainer", "testFile", 2l)))
        }
    }

    boolean mockDeleteMethod() {
        Blob.metaClass.delete = { Blob.BlobSourceOption... options ->
            return true
        }
    }

    void mockGetMethodOfStorageThrowException() {
        Storage storageInstance = Mock(Storage)
        storageInstance.get(_) >> { throw new StorageException(1, 'Test exception') }
        googleCDNFileUploaderImpl.gStorage = storageInstance
    }

    void mockGetMethodOfStorage() {
        Storage storageInstance = Mock(Storage)
        storageInstance.get(_) >> { return }
        googleCDNFileUploaderImpl.gStorage = storageInstance
    }

    void "test getBlob method for various cases"() {
        when: "Server fails to get Blob"
        mockGetMethodOfStorageThrowException()
        googleCDNFileUploaderImpl.getBlob('dummy', 'test')

        then: "Method throws exception"
        GoogleStorageException exception = thrown()
        exception.message == 'Could not find file test'

        when: "Server finds Blob"
        mockGetMethodOfStorage()
        googleCDNFileUploaderImpl.getBlob('dummy', 'test')

        then: "No exception is thrown"
        noExceptionThrown()
    }

    void "test Google Cloud Storage for delete failure"() {
        given: "mocked methods for Blob class"
        Blob.metaClass.delete = { Blob.BlobSourceOption... options ->
            return false
        }

        mockGetBlobMethod()

        when: "deleteFile() method is called"
        googleCDNFileUploaderImpl.deleteFile("dummyContainer", "testFile")

        then: "It should throw GoogleStorageException exception"
        GoogleStorageException e = thrown()
        e.message == "Could not delete file testFile from container dummyContainer"
    }

    void "test Google Cloud Storage for successful deletion"() {
        given: "mocked method for Blob class"
        mockDeleteMethod()
        mockGetBlobMethod()

        when: "deleteFile() method is called"
        googleCDNFileUploaderImpl.deleteFile("dummyContainer", "testFile")

        then: "No exception is thrown"
        noExceptionThrown()
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
        Storage storageInstance = Mock(Storage)
        storageInstance.create(_, _) >> { throw new StorageException(1, "Test exception") }
        googleCDNFileUploaderImpl.gStorage = storageInstance

        when: "uploadFile() method  is called"
        googleCDNFileUploaderImpl.uploadFile("dummyContainer", file, "test", false, 3600l)

        then: "it should throw UploadFailureException"
        UploadFailureException e = thrown()
        e.message == "Could not upload file test to container dummyContainer"

        cleanup:
        BlobId.metaClass = null
        file.delete()
    }

    void "test Google Cloud Storage for create Container failure"() {
        given: "Mocked method"
        Storage storageInstance = Mock(Storage)
        storageInstance.create(_) >> { throw new StorageException(1, "Test exception") }
        googleCDNFileUploaderImpl.gStorage = storageInstance

        when: "createContainer() method is called"
        googleCDNFileUploaderImpl.createContainer("dummyContainer")

        then: "it should throw GoogleStorageException exception"
        GoogleStorageException e = thrown()
        e.message == "Could not create container."
    }

    void "test uploadFile method for successful upload"() {
        given: "A file instance and mocked 'of' method of class BlobId"
        File file = new File('test.txt')
        file.createNewFile()
        file << 'This is a test document.'

        and: "Mocked method"
        Storage storageInstance = Mock(Storage)
        storageInstance.create(_, _) >> { return null }
        googleCDNFileUploaderImpl.gStorage = storageInstance

        when: "uploadFile method is called"
        boolean result = googleCDNFileUploaderImpl.uploadFile("dummyContainer", file, "test", false, 3600l)

        then: "Method should return true"
        result

        cleanup:
        file.delete()
    }

    void "test makeFilePublic method"() {
        expect: "Following must be true"
        !googleCDNFileUploaderImpl.makeFilePublic('dummyContainer', 'test')
    }

    void "test getPermanentURL method"() {
        when: "getPermanentURL method is called"
        String result = googleCDNFileUploaderImpl.getPermanentURL('dummyContainer', 'test')

        then: "No exception is thrown and method returns null"
        notThrown(Exception)
        result == null
    }

    void "test getTemporaryURL method to return a temporary url"() {
        when: "getTemporaryURL method is called"
        String result = googleCDNFileUploaderImpl.getTemporaryURL('dummyContainer', 'test', 3600L)

        then: "No exception is thrown and method returns null"
        noExceptionThrown()
        result == null
    }

    void "test createContainer method for successful execution"() {
        given: "Mocked method"
        Storage storageInstance = Mock(Storage)
        storageInstance.create(_) >> { return }
        googleCDNFileUploaderImpl.gStorage = storageInstance

        when: "createContainer method is called"
        boolean result = googleCDNFileUploaderImpl.createContainer('dummy')

        then: "No exception is thrown and method returns true"
        noExceptionThrown()
        result
    }

    void "test containerExist method for various cases"() {
        when: "Server fails to get container"
        mockGetMethodOfStorageThrowException()
        googleCDNFileUploaderImpl.containerExists('dummy')

        then: "Method throws exception"
        GoogleStorageException exception = thrown()
        exception.message == 'Could not find container dummy'

        when: "Server finds container"
        mockGetMethodOfStorage()
        boolean result = googleCDNFileUploaderImpl.containerExists('dummy')

        then: "No exception is thrown and method returns true"
        noExceptionThrown()
        result
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