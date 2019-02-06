package com.causecode.fileuploader.cdn.google

import com.google.cloud.storage.Blob
import com.google.cloud.storage.BlobId
import com.google.cloud.storage.BlobInfo
import com.google.cloud.storage.Storage
import com.google.cloud.storage.StorageException
import com.causecode.fileuploader.GoogleStorageException
import com.causecode.fileuploader.UploadFailureException
import grails.test.runtime.DirtiesRuntime
import spock.lang.Specification

/**
 * This class contains unit test cases for GoogleCDNFileUploaderImpl class
 */
class GoogleCDNFileUploaderImplSpec extends Specification {

    GoogleCDNFileUploaderImpl googleCDNFileUploaderImpl
    private Blob blobInstance

    def setup() {
        GoogleCredentials googleCredentialsMock = GroovyMock(GoogleCredentials, global: true)
        new GoogleCredentials() >> googleCredentialsMock

        googleCredentialsMock.storage >> {
            return Mock(Storage)
        }

        googleCDNFileUploaderImpl = new GoogleCDNFileUploaderImpl()
        googleCDNFileUploaderImpl.gStorage = Mock(Storage)

        blobInstance = new Blob(googleCDNFileUploaderImpl.gStorage,
                new BlobInfo.BuilderImpl(new BlobId('dummyContainer', 'testFile', 2L)))
    }

    void mockGetBlobMethod() {
        googleCDNFileUploaderImpl.metaClass.getBlob = { String containerName, String fileName ->
            return blobInstance
        }
    }

    boolean mockDeleteMethod(boolean boolResult) {
        Blob blobMock = GroovyMock(Blob, global: true)
        blobMock.delete(_) >> {
            return boolResult
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
        when: 'Server fails to get Blob'
        mockGetMethodOfStorageThrowException()
        googleCDNFileUploaderImpl.getBlob('dummy', 'test')

        then: 'Method throws exception'
        GoogleStorageException exception = thrown()
        exception.message == 'Could not find file test'

        when: 'Server finds Blob'
        mockGetMethodOfStorage()
        googleCDNFileUploaderImpl.getBlob('dummy', 'test')

        then: 'No exception is thrown'
        noExceptionThrown()
    }

    @DirtiesRuntime
    void "test Google Cloud Storage for delete failure"() {
        given: 'mocked methods for Blob class'
        mockGetBlobMethod()
        mockDeleteMethod(false)

        when: 'deleteFile() method is called'
        googleCDNFileUploaderImpl.deleteFile('dummyContainer', 'testFile')

        then: 'It should throw GoogleStorageException exception'
        GoogleStorageException e = thrown(GoogleStorageException)
        e.message == 'Could not delete file testFile from container dummyContainer'
    }

    @DirtiesRuntime
    void "test Google Cloud Storage for successful deletion"() {
        given: 'mocked method for Blob class'
        mockGetBlobMethod()
        mockDeleteMethod(true)

        when: 'deleteFile() method is called'
        googleCDNFileUploaderImpl.deleteFile('dummyContainer', 'testFile')

        then: 'No exception is thrown'
        noExceptionThrown()
    }

    @SuppressWarnings(['JavaIoPackageAccess'])
    void "test Google Cloud Storage for upload failure"() {
        given: 'A file instance, BlobId instance and mocked \'of\' method from BlobId class'
        File file = new File('test.txt')
        file.createNewFile()
        file << 'This is a test document.'

        BlobId blobIdInstance = new BlobId('dummyContainer', 'test', 2L)

        and: 'Mocked methods'
        GroovyMock(BlobId, global: true)
        1 * BlobId.of(_, _) >> {
            return blobIdInstance
        }

        Storage storageInstance = Mock(Storage)
        storageInstance.create(_, _) >> { throw new StorageException(1, 'Test exception') }
        googleCDNFileUploaderImpl.gStorage = storageInstance

        when: 'uploadFile() method  is called'
        googleCDNFileUploaderImpl.uploadFile('dummyContainer', file, 'test', false, 3600L)

        then: 'it should throw UploadFailureException'
        UploadFailureException e = thrown()
        e.message == 'Could not upload file test to container dummyContainer'
    }

    void "test Google Cloud Storage for create Container failure"() {
        given: 'Mocked method'
        Storage storageInstance = Mock(Storage)
        storageInstance.create(_) >> { throw new StorageException(1, 'Test exception') }
        googleCDNFileUploaderImpl.gStorage = storageInstance

        when: 'createContainer() method is called'
        googleCDNFileUploaderImpl.createContainer('dummyContainer')

        then: 'it should throw GoogleStorageException exception'
        GoogleStorageException e = thrown()
        e.message == 'Could not create container.'
    }

    @SuppressWarnings(['JavaIoPackageAccess'])
    void "test uploadFile method for successful upload"() {
        given: 'A file instance and mocked \'of\' method of class BlobId'
        File file = new File('test.txt')
        file.createNewFile()
        file << 'This is a test document.'

        and: 'Mocked method'
        Storage storageInstance = Mock(Storage)
        storageInstance.create(_, _) >> { return null }
        googleCDNFileUploaderImpl.gStorage = storageInstance

        when: 'uploadFile method is called'
        boolean result = googleCDNFileUploaderImpl.uploadFile('dummyContainer', file, 'test', false, 3600L)

        then: 'Method should return true'
        result

        cleanup:
        file.delete()
    }

    void "test makeFilePublic method"() {
        expect: 'Following must be true'
        !googleCDNFileUploaderImpl.makeFilePublic('dummyContainer', 'test')
    }

    void "test getPermanentURL method"() {
        when: 'getPermanentURL method is called'
        String result = googleCDNFileUploaderImpl.getPermanentURL('dummyContainer', 'test')

        then: 'No exception is thrown and method returns null'
        notThrown(Exception)
        result == null
    }

    void "test getTemporaryURL method to return a temporary url"() {
        when: 'getTemporaryURL method is called'
        String result = googleCDNFileUploaderImpl.getTemporaryURL('dummyContainer', 'test', 3600L)

        then: 'No exception is thrown and method returns null'
        noExceptionThrown()
        result == null
    }

    void "test createContainer method for successful execution"() {
        given: 'Mocked method'
        Storage storageInstance = Mock(Storage)
        storageInstance.create(_) >> { return }
        googleCDNFileUploaderImpl.gStorage = storageInstance

        when: 'createContainer method is called'
        boolean result = googleCDNFileUploaderImpl.createContainer('dummy')

        then: 'No exception is thrown and method returns true'
        noExceptionThrown()
        result
    }

    void "test containerExist method for various cases"() {
        when: 'Server fails to get container'
        mockGetMethodOfStorageThrowException()
        googleCDNFileUploaderImpl.containerExists('dummy')

        then: 'Method throws exception'
        GoogleStorageException exception = thrown()
        exception.message == 'Could not find container dummy'

        when: 'Server finds container'
        mockGetMethodOfStorage()
        boolean result = googleCDNFileUploaderImpl.containerExists('dummy')

        then: 'No exception is thrown and method returns true'
        noExceptionThrown()
        result
    }
}
