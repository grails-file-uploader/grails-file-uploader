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
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import grails.test.runtime.DirtiesRuntime
import grails.util.Holders
import groovy.json.JsonBuilder
import org.apache.commons.fileupload.disk.DiskFileItem
import org.apache.commons.validator.UrlValidator
import org.grails.plugins.codecs.HTMLCodec
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.web.multipart.commons.CommonsMultipartFile
import spock.lang.Specification
import spock.lang.Unroll

/**
 * This file contains unit test cases for FileUploaderService class.
 */
@TestFor(FileUploaderService)
@Mock([UFile, UFileMoveHistory])
class FileUploaderServiceSpec extends Specification implements BaseTestSetup {

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

    @DirtiesRuntime
    void "test isPublicGroup for various file groups"() {
        mockCodec(HTMLCodec)

        expect: 'Following conditions should pass'
        service.isPublicGroup('user') == true
        service.isPublicGroup('image') == false
        service.isPublicGroup('profile') == false
        service.isPublicGroup() == false
    }

    @DirtiesRuntime
    void "test moveFilesToCDN method for successfully moving a file"() {
        given: 'An instance of UFile and File'
        UFile uFileInstance = getUFileInstance(1)
        File fileInstance = getFileInstance('./temp/test.txt')
        uFileInstance.path = System.getProperty('user.dir') + '/temp/test.txt'

        and: 'Mocked method'
        FileUploaderService.metaClass.getFileFromURL = { String url, String filename ->
            return fileInstance
        }

        when: 'moveFilesToCDN method is called'
        assert uFileInstance.provider == CDNProvider.GOOGLE
        service.moveFilesToCDN([uFileInstance], CDNProvider.AMAZON)

        then: 'File would be moved successfully'
        uFileInstance.provider == CDNProvider.AMAZON

        cleanup:
        fileInstance.delete()
    }

    @DirtiesRuntime
    void "Test renewTemporaryURL method in FileUploaderService class for forceAll=false"() {
        given: 'a few instances of UFile class'
        UFile uFileInstance1 = new UFile(dateUploaded: new Date(), downloads: 0, extension: 'png', name: 'abc',
                path: 'https://xyz/abc', size: 12345, fileGroup: 'image', expiresOn: new Date() + 30,
                provider: CDNProvider.AMAZON, type: UFileType.CDN_PUBLIC).save(flush: true)
        UFile uFileInstance2 = new UFile(dateUploaded: new Date(), downloads: 0, extension: 'png', name: 'abc',
                path: 'https://xyz/abc', size: 12345, fileGroup: 'image', expiresOn: new Date() + 20,
                provider: CDNProvider.AMAZON, type: UFileType.CDN_PUBLIC).save(flush: true)
        UFile uFileInstance3 = new UFile(dateUploaded: new Date(), downloads: 0, extension: 'png', name: 'abc',
                path: 'https://xyz/abc', size: 12345, fileGroup: 'image', expiresOn: new Date() + 10,
                provider: CDNProvider.AMAZON, type: UFileType.CDN_PUBLIC).save(flush: true)
        UFile uFileInstance4 = new UFile(dateUploaded: new Date(), downloads: 0, extension: 'png', name: 'abc',
                path: 'https://xyz/abc', size: 12345, fileGroup: 'image', expiresOn: new Date(),
                provider: CDNProvider.AMAZON, type: UFileType.CDN_PUBLIC).save(flush: true)

        assert UFile.count() == 4

        and: 'Mocked AmazonCDNFileUploaderImpl\'s methods'
        mockAuthenticateMethod()

        when: 'renewTemporaryURL method is called'
        service.renewTemporaryURL()
        String uFilePath = uFileInstance4.path

        then: 'It should only change image path of uFileInstance4'
        uFileInstance1.path == 'https://xyz/abc'
        uFileInstance2.path == 'https://xyz/abc'
        uFileInstance3.path == 'https://xyz/abc'
        uFilePath == 'http://fixedURL.com'

        when: 'renewTemporaryURL method is called'
        uFileInstance4.path = 'https://xyz/abc'
        uFileInstance4.save(flush: true)

        assert uFileInstance4.path == 'https://xyz/abc'
        service.renewTemporaryURL(true)

        then: 'It should renew the image path of all the Instance'
        uFileInstance1.path == 'http://fixedURL.com'
        uFileInstance2.path == 'http://fixedURL.com'
        uFileInstance3.path == 'http://fixedURL.com'
        uFileInstance4.path == 'http://fixedURL.com'
    }

    @Unroll
    @DirtiesRuntime
    void "test saveFile for uploading files for CDNProvider #provider"() {
        given: 'A file instance'
        File file = getFileInstance('./temp/test.txt')

        and: 'Mocked method'
        mockAuthenticateMethod()

        when: 'The saveFile method is called'
        UFile ufileInstancefile = service.saveFile(fileGroup, file, 'test')

        then: 'UFile instance should be successfully saved'
        ufileInstancefile.id
        ufileInstancefile.provider == provider
        ufileInstancefile.extension == 'txt'
        ufileInstancefile.container == 'causecode-test'
        ufileInstancefile.fileGroup == fileGroup

        file.delete()

        where:
        fileGroup | provider
        'testAmazon' | CDNProvider.AMAZON
        'testGoogle' | CDNProvider.GOOGLE
    }

    @DirtiesRuntime
    void "test saveFile method in FileUploaderService when file upload fails"() {
        given: 'A file instance and mocked method \'uploadFile\' of class GoogleCDNFileUploaderImpl'
        File file = getFileInstance('./temp/test.txt')

        GoogleCDNFileUploaderImpl.metaClass.uploadFile = {
                String containerName, File fileToUpload, String fileName, boolean makePublic, long maxAge ->
            throw new UploadFailureException(fileName, containerName, new Throwable())
        }

        when: 'The saveFile() method is called'
        service.saveFile('testGoogle', file, 'test')

        then: 'It should throw UploadFailureException'
        UploadFailureException e = thrown()
        e.message.contains('Could not upload file')

        cleanup:
        file.delete()
    }

    @DirtiesRuntime
    void "test uploadFileToCloud method for successful execution"() {
        given: 'A file instance and mocked method \'uploadFile\' of class GoogleCDNFileUploaderImpl'
        File file = getFileInstance('./temp/test.txt')
        FileGroup fileGroupInstance = new FileGroup('testGoogle')
        Holders.grailsApplication.config.fileuploader.groups.testGoogle.makePublic = true

        and: 'Mocked methods'
        mockGetPermanentURL()

        when: 'The uploadFileToCloud method is called'
        String resultPath = service.uploadFileToCloud([fileName: 'test', fileExtension: '.txt'],
                fileGroupInstance, file)

        then: 'It should return path of uploaded file'
        resultPath == 'http://fixedURL.com'

        cleanup:
        file.delete()
    }

    @DirtiesRuntime
    void "test moveFilesToCDN method for making a file public while moving and error occurs"() {
        given: 'An instance of UFile and File'
        UFile uFileInstance = getUFileInstance(1)
        uFileInstance.type = UFileType.CDN_PUBLIC
        File fileInstance = getFileInstance('./temp/test.txt')
        uFileInstance.path = System.getProperty('user.dir') + '/temp/test.txt'

        and: 'Mocked method'
        FileUploaderService.metaClass.getFileFromURL = { String url, String filename ->
            return fileInstance
        }

        when: 'moveFilesToCDN method is called'
        assert uFileInstance.provider == CDNProvider.GOOGLE
        assert uFileInstance.type == UFileType.CDN_PUBLIC
        service.moveFilesToCDN([uFileInstance], CDNProvider.AMAZON, true)

        then: 'File move history would contain failure status'
        UFileMoveHistory uFileMoveHistoryInstance = UFileMoveHistory.get(1)
        uFileMoveHistoryInstance.status == MoveStatus.FAILURE

        cleanup:
        fileInstance.delete()
    }

    @DirtiesRuntime
    void "test moveFilesToCDN method for making a file public while moving and no error occurs"() {
        given: 'An instance of UFile and File'
        UFile uFileInstance = getUFileInstance(1)
        File fileInstance = getFileInstance('./temp/test.txt')
        uFileInstance.path = System.getProperty('user.dir') + '/temp/test.txt'

        and: 'Mocked method'
        FileUploaderService.metaClass.getFileFromURL = { String url, String filename ->
            return fileInstance
        }

        mockGetPermanentURL()

        when: 'moveFilesToCDN method is called'
        assert uFileInstance.provider == CDNProvider.GOOGLE
        assert uFileInstance.type == UFileType.LOCAL
        service.moveFilesToCDN([uFileInstance], CDNProvider.AMAZON, true)

        then: 'File would be made public'
        uFileInstance.type == UFileType.CDN_PUBLIC
        uFileInstance.provider == CDNProvider.AMAZON

        cleanup:
        fileInstance.delete()
    }

    @DirtiesRuntime
    void "test moveFilesToCDN method for failure cases"() {
        given: 'An instance of UFile and File'
        UFile uFileInstance = getUFileInstance(1)
        File fileInstance = getFileInstance('./temp/test.txt')

        when: 'moveFilesToCDN method is called and file does not exist'
        FileUploaderService.metaClass.getFileFromURL = { String url, String filename ->
            return fileInstance
        }
        mockExistMethod(false)

        def failedUploadList = service.moveFilesToCDN([uFileInstance], CDNProvider.AMAZON)

        then: 'File would be moved successfully and method would return empty list for failed uploads'
        failedUploadList == []

        cleanup:
        fileInstance.delete()
    }

    @DirtiesRuntime
    void "test moveToNewCDN method for various cases"() {
        given: 'Mocked method'
        FileUploaderService.metaClass.moveFilesToCDN = { List<UFile> uFileList, CDNProvider toCDNProvider ->
            return
        }

        when: 'moveToNewCDN method is called and parameters are invalid'
        def result = service.moveToNewCDN(null, null)

        then: 'method returs false'
        !result

        when: 'Valid parameters are received'
        result = service.moveToNewCDN(CDNProvider.GOOGLE, 'dummy')

        then: 'Method returns true'
        result
    }

    @DirtiesRuntime
    void "test updateAllUFileCacheHeader for various cases"() {
        given: 'Mocked method'
        mockAuthenticateMethod()

        UFile.metaClass.static.withCriteria = { Closure closure ->
            assert closure != null
            new JsonBuilder() closure
            return getUFileInstance(1)
        }

        AmazonCDNFileUploaderImpl.metaClass.updatePreviousFileMetaData = { String containerName, String fileName,
                Boolean makePublic, long maxAge ->
            return }

        when: 'updateAllUFileCacheHeader method is called and provider is not AMAZON'
        def result = service.updateAllUFileCacheHeader(CDNProvider.GOOGLE)

        then: 'Method returns null'
        result == null

        when: 'updateAllUFileCacheHeader method is called and method executes successfully'
        service.updateAllUFileCacheHeader()

        then: 'No exceptions are thrown'
        noExceptionThrown()
    }

    @DirtiesRuntime
    void "test getFileFromURL method to return a file"() {
        when: 'getFileFromURL method is called'
        File responseFile = service.getFileFromURL('http://causecode.com/test', 'test')

        then: 'Method returns a file'
        responseFile != null
    }

    @DirtiesRuntime
    void "test cloneFile method for various cases"() {
        given: 'An instance of UFile and File'
        UFile ufIleInstance = getUFileInstance(1)
        File fileInstance = getFileInstance('./temp/test.txt')

        and: 'Mocked method'
        FileUploaderService.metaClass.saveFile = { String group, def file, String customFileName = '',
                Object userInstance = null, Locale locale = null ->
            return ufIleInstance
        }

        when: 'cloneFile method is called and uFileInstance is missing'
        def result = service.cloneFile('testGoogle', null)

        then: 'Method returns null'
        result == null

        when: 'cloneFile method is called for valid parameters and UFile is LOCAL type'
        ufIleInstance.type = UFileType.LOCAL
        result = service.cloneFile('testGoogle', ufIleInstance, 'test')

        then: 'The method returns a valid file'
        result.name == 'test-file-1'

        cleanup:
        fileInstance.delete()
    }

    @DirtiesRuntime
    void "test cloneFile method for LOCAL type file"() {
        given: 'An instance of UFile and File'
        UFile ufIleInstance = getUFileInstance(1)
        File fileInstance = getFileInstance('./temp/test.txt')
        ufIleInstance.path = 'http://causecode.com/test'

        and: 'Mocked method'
        FileUploaderService.metaClass.saveFile = { String group, def file, String customFileName = '',
                Object userInstance = null, Locale locale = null ->
            return ufIleInstance
        }

        when: 'cloneFile method is called for valid parameters and UFile is not LOCAL type'
        ufIleInstance.type = UFileType.CDN_PUBLIC

        UrlValidator.metaClass.isValid = { String value ->
            return true
        }

        def result = service.cloneFile('testGoogle', ufIleInstance, 'test')

        then: 'The method returns a valid file'
        result.name == 'test-file-1'

        cleanup:
        fileInstance.delete()
    }

    @DirtiesRuntime
    void "test resolvePath for various cases"() {
        given: 'An instance of UFile'
        UFile uFileInstance = getUFileInstance(1)

        when: 'resolvePath method is called and invalid params are passed'
        def result = service.resolvePath(null)

        then: 'Method returns null'
        result == ''

        when: 'ufileInstance type is LOCAL'
        uFileInstance.type = UFileType.LOCAL
        result = service.resolvePath(uFileInstance)

        then: 'Following condition should be true'
        result == "/file-uploader/show/$uFileInstance.id"

        when: 'UFile type is public'
        uFileInstance.type = UFileType.CDN_PUBLIC
        result = service.resolvePath(uFileInstance)

        then: 'Method returns UFile instance path'
        result == uFileInstance.path
    }

    @DirtiesRuntime
    void "test fileForUFile method when UFile is public type"() {
        given: 'An instance of File and UFile'
        File fileInstance = getFileInstance('./temp/test.txt')
        UFile uFileInstance = getUFileInstance(1)

        and: 'Mocked methods'
        FileUploaderService.metaClass.getFileFromURL = { String url, String filename ->
            return fileInstance
        }

        mockExistMethod(true)

        when: 'UFile is PUBLIC type and file exist'
        uFileInstance.path = System.getProperty('user.dir') + '/temp/test.txt'
        uFileInstance.type = UFileType.CDN_PUBLIC
        def result = service.fileForUFile(uFileInstance, null)

        then: 'Method should return a file and downloads for the file should increase'
        uFileInstance.downloads == 2
        result != null

        cleanup:
        fileInstance.delete()
    }

    @DirtiesRuntime
    void "test deleteFileForUFile method for various cases"() {
        given: 'A UFile instance'
        UFile uFileInstance = getUFileInstance(1)
        File fileInstance = getFileInstance('./temp/test.txt')
        uFileInstance.path = System.getProperty('user.dir') + '/temp/test.txt'

        and: 'Mocked method'
        File.metaClass.delete = {
            return false
        }

        when: 'deleteFileForUFile method is called and file does not exist'
        mockExistMethod(false)
        def result = service.deleteFileForUFile(uFileInstance)

        then: 'Method returns false'
        !result

        when: 'File exists but deletion fails'
        mockExistMethod(true)
        uFileInstance.path = System.getProperty('user.dir') + '/temp/test.txt'
        service.deleteFileForUFile(uFileInstance)

        then: 'File would not be deleted'
        fileInstance.exists()

        cleanup:
        fileInstance.delete()
    }

    @DirtiesRuntime
    void "test deleteFileForUFile method for PUBLIC file"() {
        given: 'A UFile instance'
        UFile uFileInstance = getUFileInstance(1)
        uFileInstance.type = UFileType.CDN_PUBLIC

        and: 'Mocked method'
        GoogleCDNFileUploaderImpl.metaClass.deleteFile = { String containerName, String fileName ->
        }

        when: 'deleteFileForUFile method is called and method executes successfully'
        def response = service.deleteFileForUFile(uFileInstance)

        then: 'Method returns true and no exceptions are thrown'
        noExceptionThrown()
        response

        when: 'deleteFileForUFile method is called and class does not exist'
        uFileInstance.provider = CDNProvider.RACKSPACE
        service.deleteFileForUFile(uFileInstance)

        then: 'Method returns true and no exceptions are thrown'
        ProviderNotFoundException e = thrown()
        e.message == 'Provider RACKSPACE not found.'
    }

    @DirtiesRuntime
    @SuppressWarnings(['JavaIoPackageAccess'])
    void "test deleteFileForUFile method for LOCAL file"() {
        given: 'A UFile and a File instance'
        UFile uFileInstance = getUFileInstance(1)
        uFileInstance.path = System.getProperty('user.dir') + '/temp/testDir/test.txt'
        new File('./temp/testDir').mkdir()
        uFileInstance.type = UFileType.LOCAL

        and: 'Mocked method'
        mockExistMethod(true)
        File.metaClass.delete = {
            return true
        }

        when: 'deleteFileForUFile method is called and method executes successfully'
        service.deleteFileForUFile(uFileInstance)

        then: 'No exceptions are thrown'
        noExceptionThrown()
    }

    @DirtiesRuntime
    void "test deleteFileForUFile method for LOCAL file when parent folder not empty"() {
        given: 'A UFile and a File instance'
        UFile uFileInstance = getUFileInstance(1)
        uFileInstance.type = UFileType.LOCAL

        and: 'Mocked method'
        mockExistMethod(true)
        File.metaClass.delete = {
            return true
        }

        when: 'deleteFileForUFile method is called and method executes successfully'
        service.deleteFileForUFile(uFileInstance)

        then: 'No exceptions are thrown'
        noExceptionThrown()
    }

    @DirtiesRuntime
    void "test deleteFile method for various cases"() {
        given: 'An instance of UFile'
        UFile uFileInstance = getUFileInstance(1)
        getUFileInstance(2)

        when: 'deleteFile method is called and File is nt found'
        def result = service.deleteFile(null)

        then: 'Method returns false'
        !result

        when: 'deleteFile method is called and file is deleted successfully'
        assert UFile.count() == 2
        result = service.deleteFile(uFileInstance.id)

        then: 'Method returns true'
        UFile.count() == 1
        result
    }

    @DirtiesRuntime
    void "test renewTemporaryURL method when fileUploaderInstance is null"() {
        given: 'Mocked method'
        FileUploaderService.metaClass.getProviderInstance = { String name ->
            return null
        }
        when: 'renewTemporaryURL method is called'
        def result = service.renewTemporaryURL()

        then: 'Method returns null'
        result == null
    }

    @DirtiesRuntime
    void "test getProviderInstance method"() {
        when: 'getProviderInstance method is called and class does not exist'
        service.getProviderInstance('test')

        then: 'Method should throw exception'
        ProviderNotFoundException e = thrown()
        e.message == 'Provider test not found.'

        when: 'getProviderInstance method is called and class exist'
        def result = service.getProviderInstance('Amazon')

        then: 'No exception is thrown'
        noExceptionThrown()
        result != null
    }

    @DirtiesRuntime
    void "test saveFile method for various cases"() {
        given: 'An instance of File'
        File fileInstance = getFileInstance('./temp/test.txt')

        DiskFileItem fileItem = getFileItem(fileInstance)
        CommonsMultipartFile commonsMultipartFileInstance = new CommonsMultipartFile(fileItem)

        and: 'Mocked methods'
        mockAuthenticateMethod()
        mockExistMethod(false)

        when: 'saveFile method is hit'
        def result = service.saveFile('testGoogle', commonsMultipartFileInstance, 'test')

        then: 'Method should return null'
        result == null

        when: 'saveFile is called and provider is not specified'
        mockGetFileNameAndExtensions()
        FileGroup.metaClass.getCdnProvider = { return null }

        service.saveFile('testGoogle', commonsMultipartFileInstance, 'test')

        then: 'Method should throw StorageConfigurationException'
        StorageConfigurationException e = thrown()
        e.message == 'Provider not defined in the Config. Please define one.'

        cleanup:
        fileInstance.delete()
    }

    @DirtiesRuntime
    void "test saveFile when provider is LOCAL"() {
        given: 'File instance'
        File fileInstance = getFileInstance('./temp/test.txt')

        DiskFileItem fileItem = getFileItem(fileInstance)
        CommonsMultipartFile commonsMultipartFileInstance = new CommonsMultipartFile(fileItem)

        and: 'Mocked methods'
        mockGetFileNameAndExtensions()

        when: 'saveFile method is called and file gets saved'
        def result = service.saveFile('testLocal', fileInstance, 'test')

        then: 'Method should return saved UFile instance'
        result.id != null

        when: 'saveFile method is called and error occures while saving file'
        FileGroup.metaClass.getFileNameAndExtensions = { def file, String customFileName ->
            return [fileName: null, fileExtension: 'txt', customFileName: 'unit-test', empty: false,
                    fileSize: 38L]
        }
        result = service.saveFile('testLocal', commonsMultipartFileInstance, 'test')

        then: 'File would not be saved'
        result.id == null

        cleanup:
        fileInstance.delete()
    }

    @DirtiesRuntime
    void "test moveFailedFilesToCDN for various cases"() {
        given: 'Instances of UFileMoveHistory'
        UFileMoveHistory uFileMoveHistoryInstance = getUFileMoveHistoryInstance(1)

        FileUploaderService.metaClass.moveFilesToCDN = { List<UFile> uFileList, CDNProvider toCDNProvider,
            boolean makePublic = false ->
            return
        }

        when: 'moveFailedFilesToCDN method is called and failedList containes no UFiles'
        service.moveFailedFilesToCDN()

        then: 'No exception is thrown'
        noExceptionThrown()

        when: 'moveFailedFilesToCDN method is called and failedList containes UFiles'
        UFileMoveHistory.metaClass.static.withCriteria = { Closure closure ->
            assert closure != null
            new JsonBuilder() closure
            return [uFileMoveHistoryInstance]
        }

        service.moveFailedFilesToCDN()

        then: 'No exception is thrown'
        noExceptionThrown()
    }

    @DirtiesRuntime
    void "test ufileById method for various cases"() {
        given: 'An instance of UFile'
        UFile uFileInstance = getUFileInstance(1)
        Locale locale = LocaleContextHolder.locale

        and: 'Mocked method'
        MessageSource testInstance = Mock(MessageSource)
        testInstance.getMessage(_, _, _) >> { 'File not found' }
        service.messageSource = testInstance

        when: 'ufileById method is called and UFile exists'
        def result = service.ufileById(1, locale)

        then: 'Method returns ufile instance'
        result == uFileInstance

        when: 'uFIleById method is called and ufile does not exist'
        service.ufileById(2, locale)

        then: 'Method throws FileNotFoundException'
        FileNotFoundException e =thrown()
        e.message == 'File not found'
    }

    @DirtiesRuntime
    void "test fileForUFile method when file does not exist"() {
        given: 'An instance of File and UFile'
        File fileInstance = getFileInstance('./temp/test.txt')
        UFile uFileInstance = getUFileInstance(1)

        and: 'Mocked methods'
        FileUploaderService.metaClass.getFileFromURL = { String url, String filename ->
            return fileInstance
        }

        mockExistMethod(false)

        MessageSource testInstance = Mock(MessageSource)
        testInstance.getMessage(_, _, _) >> { 'File not found' }
        service.messageSource = testInstance

        when: 'UFile is PUBLIC type and file exist'
        uFileInstance.path = System.getProperty('user.dir') + '/temp/test.txt'
        uFileInstance.type = UFileType.CDN_PUBLIC
        service.fileForUFile(uFileInstance, null)

        then: 'Method should throw IOException'
        IOException e = thrown()
        e.message == 'File not found'

        cleanup:
        fileInstance.delete()
    }

    @DirtiesRuntime
    void "test saveFile method when validation fails"() {
        given: 'An instance of File'
        File fileInstance = getFileInstance('./temp/test.txt')

        and: 'Mocked method'
        FileGroup.metaClass.scopeFileName = { Object userInstance, Map fileDataMap, String group,
                Long currentTimeMillis ->
            throw new StorageConfigurationException('Container name not defined in the Config. Please define one.')
        }

        when: 'saveFile methods is called and exception is thrown while modifying fileName'
        service.saveFile('testGoogle', fileInstance)

        then: 'StorageConfigurationException is thrown'
        StorageConfigurationException e = thrown()
        e.message == 'Container name not defined in the Config. Please define one.'

        when: 'saveFile method is called and FileSize is greater than permitted value'
        FileGroup.metaClass.validateFileSize = { Map fileDataMap, Locale locale ->
            throw new StorageConfigurationException('File too big.')
        }
        service.saveFile('testGoogle', fileInstance)

        then: 'Method throws StorageConfigurationException'
        StorageConfigurationException exception = thrown()
        exception.message == 'File too big.'

        cleanup:
        fileInstance.delete()
    }
}
