package com.causecode.fileuploader.cdn.google

import com.google.cloud.storage.Storage
import com.causecode.fileuploader.StorageConfigurationException
import com.google.cloud.storage.StorageOptions
import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import grails.util.Holders
import spock.lang.Specification
import spock.lang.Unroll

/**
 * This class contains unit test cases for GoogleCredentials class
 */
@TestMixin(GrailsUnitTestMixin)
class GoogleCredentialsSpec extends Specification {

    ConfigObject storageProviderGoogle

    void setup() {
        storageProviderGoogle = Holders.grailsApplication.config.fileuploader.storageProvider.google
    }

    void cleanup() {
        // Restore the config object.
        Holders.grailsApplication.config.fileuploader.storageProvider.google = storageProviderGoogle
    }

    void "test getStorage method when authentication fails for authenticateUsingEnvironmentVariable() method"() {
        given: 'Config object is set to null'
        GoogleCredentials googleCredentials = new GoogleCredentials()
        Holders.grailsApplication.config.fileuploader.storageProvider.google = null

        and: 'Mocked StorageOptions getDefaultInstance method'
        GroovyMock(StorageOptions, global: true)
        StorageOptions.defaultInstance >> {
            throw new IllegalArgumentException('GCS Authentication failed due to bad configuration')
        }

        when: 'getStorage method is called'
        googleCredentials.storage

        then: 'Method should throw StorageConfigurationException'
        StorageConfigurationException exception = thrown()
        exception.message == 'GCS Authentication failed due to bad configuration'
    }

    void 'test credentials initialization from config object'() {
        when: 'Credentials are read from the config object'
        GoogleCredentials googleCredentials = new GoogleCredentials()
        googleCredentials.initializeGoogleCredentialsFromConfig()

        then: 'Fields should be successfully initialized'
        googleCredentials.projectId == storageProviderGoogle.project_id
        googleCredentials.type == storageProviderGoogle.type
    }

    void 'test credentials initialization when config object is empty'() {
        given: 'Config object is set to null'
        GoogleCredentials googleCredentials = new GoogleCredentials()
        Holders.grailsApplication.config.fileuploader.storageProvider.google = null

        when: 'Credentials are read from the config object'
        googleCredentials.initializeGoogleCredentialsFromConfig()

        then: 'StorageConfigurationException should be thrown'
        StorageConfigurationException exception = thrown(StorageConfigurationException)
        exception.message == 'No configuration found for storage provider Google.'
    }

    void 'test credentials initialization when config object does not contain the project_id'() {
        given: 'project_id is set to blank string'
        Holders.grailsApplication.config.fileuploader.storageProvider.google.project_id = ''

        when: 'Credentials are read from the config object'
        GoogleCredentials googleCredentials = new GoogleCredentials()
        googleCredentials.initializeGoogleCredentialsFromConfig()

        then: 'StorageConfigurationException should be thrown'
        StorageConfigurationException exception = thrown(StorageConfigurationException)
        exception.message == 'Project Id is required for storage provider Google.'
    }

    @SuppressWarnings(['JavaIoPackageAccess'])
    void 'test authentication by reading path of json file from config object'() {
        given: 'auth variable is set to point to testkey.json file'
        File file = new File('')
        String testFilePath = file.absolutePath +
                '/src/test/groovy/com/causecode/fileuploader/cdn/google/testkey.json'
        file.delete()
        // Only auth variable should be present. To confirm credentials are read from the file and not the config.
        Holders.grailsApplication.config.fileuploader.storageProvider.google = [:]
        Holders.grailsApplication.config.fileuploader.storageProvider.google.authFile = testFilePath
        Holders.grailsApplication.config.fileuploader.storageProvider.google.project_id = 'test_id'

        when: 'Credentials are read from the test json key file'
        GoogleCredentials googleCredentials = new GoogleCredentials()
        googleCredentials.initializeGoogleCredentialsFromConfig()
        Storage storage = googleCredentials.authenticateUsingKeyFileFromConfig()

        then: 'Authentication should be successful and no Exception is thrown'
        notThrown(Exception)
        googleCredentials.projectId == 'test_id'
        storage != null // Only writing storage will do but that is not readable.
    }

    @Unroll
    void 'test authentication by reading path of json file from config object when path is #filePath'() {
        given: 'auth is set to blank/incorrect path'
        Holders.grailsApplication.config.fileuploader.storageProvider.google = [:]
        Holders.grailsApplication.config.fileuploader.storageProvider.google.authFile = filePath
        Holders.grailsApplication.config.fileuploader.storageProvider.google.project_id = 'test_id'

        when: 'File path is read from the config object'
        GoogleCredentials googleCredentials = new GoogleCredentials()
        googleCredentials.initializeGoogleCredentialsFromConfig()
        Storage storage = googleCredentials.authenticateUsingKeyFileFromConfig()

        then: 'StorageConfigurationException/IOException should be thrown'
        Exception exception = thrown(exceptionClass)
        exception.message == message
        storage == null // !storage will do but written this way for readability.

        where:
        filePath | exceptionClass | message
        '' | IllegalArgumentException | 'JSON Key file path for storage provider Google not found.'
        '/incorrect/path/to/testkey.json' | IOException | '/incorrect/path/to/testkey.json (No such file or directory)'
    }

    void 'test authentication for failure when credentials are read directly from the configuration object'() {
        given: 'Google storage provider configuration'
        Holders.grailsApplication.config.fileuploader.storageProvider.google = null
        Holders.grailsApplication.config.fileuploader.storageProvider.google.project_id = 'test_id'

        when: 'Credentials are read directly from the config object'
        GoogleCredentials googleCredentials = new GoogleCredentials()
        googleCredentials.initializeGoogleCredentialsFromConfig()
        Storage storage = googleCredentials.authenticateUsingValuesFromConfig()

        then: 'IOException should be thrown'
        IOException exception = thrown(IOException)
        exception.message == 'Invalid PKCS#8 data.' // Due to absence of private_key.
        storage == null
    }

    void 'test authentication for success when credentials are read directly from the configuration object'() {
        when: 'Credentials are read directly from the config object'
        Holders.grailsApplication.config.fileuploader.storageProvider.google.project_id = 'test_id'
        GoogleCredentials googleCredentials = new GoogleCredentials()
        googleCredentials.initializeGoogleCredentialsFromConfig()
        Storage storage = googleCredentials.authenticateUsingValuesFromConfig()

        then: 'Authentication should be successful'
        storage != null
    }

    void "test authenticateUsingEnvironmentVariable method for failure case"() {
        given: 'Mocked StorageOptions getDefaultInstance method'
        GroovyMock(StorageOptions, global: true)
        StorageOptions.defaultInstance >> {
            throw new IllegalArgumentException('A project ID is required for this service but could not ' +
                    'be determined from the builder or')
        }

        when: 'authenticateUsingEnvironmentVariable method is called'
        GoogleCredentials googleCredentials = new GoogleCredentials()
        googleCredentials.authenticateUsingEnvironmentVariable()

        then: 'Authentication should fail and exception is thrown'
        IllegalArgumentException e = thrown(IllegalArgumentException)
        e.message.contains('A project ID is required for this service but could not be determined from the builder or')
    }

    void "test authenticateUsingEnvironmentVariable method for success case"() {
        when: 'authenticateUsingEnvironmentVariable method is called'
        GoogleCredentials googleCredentials = new GoogleCredentials()
        Storage storage = googleCredentials.authenticateUsingEnvironmentVariable()

        then: 'Storage instance will be returned'
        noExceptionThrown()
        storage != null
    }

    void "test getStorage method for success case"() {
        given: 'An instance of GoogleCredentials'
        GoogleCredentials googleCredentials = new GoogleCredentials()

        when: 'getStorage method is called'
        Storage storage = googleCredentials.storage

        then: 'Storage instance will be returned'
        noExceptionThrown()
        storage != null
    }

    @SuppressWarnings('JavaIoPackageAccess')
    void "test getStorage method when authenticateUsingKeyFileFromConfig method is called"() {
        given: 'An instance of File, and Config object is set to null'
        File file = new File('')
        String testFilePath = file.absolutePath +
                '/src/test/groovy/com/causecode/fileuploader/cdn/google/testkey.json'

        GoogleCredentials googleCredentials = new GoogleCredentials()

        Holders.grailsApplication.config.fileuploader.storageProvider.google.client_id = null
        Holders.grailsApplication.config.fileuploader.storageProvider.google.client_email = null
        Holders.grailsApplication.config.fileuploader.storageProvider.google.private_key = null
        Holders.grailsApplication.config.fileuploader.storageProvider.google.private_key_id = null

        Holders.grailsApplication.config.fileuploader.storageProvider.google.authFile = testFilePath

        when: 'getStorage method is called'
        Storage storage = googleCredentials.storage

        then: 'Storage instance will be returned'
        noExceptionThrown()
        storage != null

        cleanup:
        file.delete()
    }
}
