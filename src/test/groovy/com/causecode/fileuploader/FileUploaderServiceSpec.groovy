/*
 * Copyright (c) 2016, CauseCode Technologies Pvt Ltd, India.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or
 * without modification, are not permitted.
 */
package com.causecode.fileuploader

import com.causecode.fileuploader.util.checksum.Algorithm
import com.causecode.fileuploader.util.checksum.exceptions.DuplicateFileException
import grails.buildtestdata.mixin.Build
import grails.test.mixin.TestFor
import grails.test.runtime.DirtiesRuntime
import grails.util.Holders
import groovy.json.JsonBuilder
import org.apache.commons.fileupload.disk.DiskFileItem
import org.apache.commons.validator.UrlValidator
import org.grails.plugins.codecs.HTMLCodec
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.multipart.commons.CommonsMultipartFile
import org.springframework.web.multipart.support.StandardMultipartHttpServletRequest.StandardMultipartFile
import spock.lang.Unroll
import spock.util.mop.ConfineMetaClassChanges

import javax.servlet.http.Part

/**
 * This file contains unit test cases for FileUploaderService class.
 */
// Suppressed Methods counts since this class contains more than 30 methods.
@ConfineMetaClassChanges([FileUploaderService, File])
@TestFor(FileUploaderService)
@Build([UFile, UFileMoveHistory])
@SuppressWarnings('MethodCount')
class FileUploaderServiceSpec extends BaseFileUploaderServiceSpecSetup {

    def setup() {
        UtilitiesService utilitiesService = Mock(UtilitiesService)
        utilitiesService.grailsApplication = Holders.grailsApplication
        service.utilitiesService = utilitiesService
    }

    void "test isPublicGroup for various file groups"() {
        mockCodec(HTMLCodec)

        expect: 'Following conditions should pass'
        service.isPublicGroup('user') == true
        service.isPublicGroup('image') == false
        service.isPublicGroup('profile') == false
        service.isPublicGroup() == false
    }

    void "test moveFilesToCDN method for successfully moving a file"() {
        given: 'An instance of UFile and File'
        UFile uFileInstance = UFile.build()
        File fileInstance = getFileInstance('/tmp/test.txt')

        and: 'Mocked method'
        service.metaClass.getFileFromURL = { String url, String filename ->
            return fileInstance
        }

        mockGetProviderInstance('google')

        mockUploadFileMethod(true)

        when: 'moveFilesToCDN method is called'
        assert uFileInstance.provider == CDNProvider.GOOGLE
        service.moveFilesToCDN([uFileInstance], CDNProvider.AMAZON)

        then: 'File would be moved successfully'
        uFileInstance.provider == CDNProvider.AMAZON

        cleanup:
        fileInstance?.delete()
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
        mockGetTemporaryURL()

        and: 'Mocked getProviderInstance method'
        service.utilitiesService.getProviderInstance(_) >> { String providerName ->
            return amazonCDNFileUploaderInstance
        }

        when: 'renewTemporaryURL method is called'
        service.renewTemporaryURL()
        String uFilePath = uFileInstance4.path

        then: 'It should only change image path of uFileInstance4'
        uFileInstance1.path == 'https://xyz/abc'
        uFileInstance2.path == 'https://xyz/abc'
        uFileInstance3.path == 'https://xyz/abc'
        uFilePath != 'https://xyz/abc'

        when: 'renewTemporaryURL method is called'
        uFileInstance4.path = 'https://xyz/abc'
        uFileInstance4.save(flush: true)

        assert uFileInstance4.path == 'https://xyz/abc'
        service.renewTemporaryURL(true)

        then: 'It should renew the image path of all the Instance'
        uFileInstance1.path != 'https://xyz/abc'
        uFileInstance2.path != 'https://xyz/abc'
        uFileInstance3.path != 'https://xyz/abc'
        uFileInstance4.path != 'https://xyz/abc'
    }

    @Unroll
    void "test saveFile for uploading files for CDNProvider #provider"() {
        given: 'A file instance'
        File file = getFileInstance('/tmp/test.txt')

        and: 'Mocked method'
        mockAuthenticateMethod()
        mockGetFileNameAndExtensions()
        mockUploadFileMethod(true)
        service.utilitiesService.getProviderInstance(_) >> { String providerName ->
            providerName == 'GOOGLE' ? googleCDNFileUploaderImplMock : amazonCDNFileUploaderImplMock
        }

        new FileGroup(_) >> fileGroupMock
        fileGroupMock.cdnProvider >> provider
        fileGroupMock.groupConfig >> [storageTypes: 'CDN']

        when: 'The saveFile method is called'
        UFile ufileInstancefile = service.saveFile(fileGroup, file, 'test')

        then: 'UFile instance should be successfully saved'
        ufileInstancefile.provider == provider
        ufileInstancefile.extension == 'txt'
        ufileInstancefile.container == 'causecode-test'
        ufileInstancefile.fileGroup == fileGroup

        file.delete()

        where:
        fileGroup    | provider
        'testAmazon' | CDNProvider.AMAZON
        'testGoogle' | CDNProvider.GOOGLE
    }

    void "test saveFile method in FileUploaderService when file upload fails"() {
        given: 'A file instance and mocked method \'uploadFile\' of class GoogleCDNFileUploaderImpl'
        File file = getFileInstance('/tmp/test.txt')

        1 * googleCDNFileUploaderImplMock.uploadFile(_, _, _, _, _) >> {
            String containerName, File fileToUpload, String fileName, boolean makePublic, long maxAge ->
                throw new UploadFailureException(fileName, containerName, new Throwable())
        }

        mockGetProviderInstance('google')

        and: 'Mocked FileGroup class method call'
        new FileGroup(_) >> fileGroupMock
        fileGroupMock.cdnProvider >> CDNProvider.GOOGLE
        fileGroupMock.groupConfig >> [storageTypes: 'CDN']
        mockGetFileNameAndExtensions()

        when: 'The saveFile() method is called'
        service.saveFile('testGoogle', file, 'test')

        then: 'It should throw UploadFailureException'
        UploadFailureException e = thrown()
        e.message.contains('Could not upload file')

        cleanup:
        file?.delete()
    }

    void "test uploadFileToCloud method for successful execution"() {
        given: 'A file instance and mocked method \'uploadFile\' of class GoogleCDNFileUploaderImpl'
        File file = getFileInstance('/tmp/test.txt')
        fileGroupMock.groupName >> 'testGoogle'
        FileGroup fileGroupInstance = fileGroupMock
        Holders.grailsApplication.config.fileuploader.groups.testGoogle.makePublic = true

        and: 'Mocked methods'
        fileGroupMock.cdnProvider >> CDNProvider.GOOGLE
        mockGetProviderInstance('google')
        mockUploadFileMethod(true)
        mockGetPermanentURL()

        when: 'The uploadFileToCloud method is called'
        String resultPath = service.uploadFileToCloud([fileName: 'test', fileExtension: '.txt'],
                fileGroupInstance, file)

        then: 'It should return path of uploaded file'
        resultPath == 'http://fixedURL.com'

        cleanup:
        file?.delete()
    }

    void "test moveFilesToCDN method for making a file public while moving and error occurs"() {
        given: 'An instance of UFile and File'
        UFile uFileInstance = UFile.build(type: UFileType.CDN_PUBLIC)
        File fileInstance = getFileInstance('/tmp/test.txt')
        assert uFileInstance.type == UFileType.CDN_PUBLIC

        and: 'Mocked method'
        service.metaClass.getFileFromURL = { String url, String filename ->
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
        fileInstance?.delete()
    }

    void "test moveFilesToCDN method for making a file public while moving and no error occurs"() {
        given: 'An instance of UFile and File'
        UFile uFileInstance = UFile.build()
        File fileInstance = getFileInstance('/tmp/test.txt')

        and: 'Mocked method'
        mockGetProviderInstance('google')
        mockUploadFileMethod(true)
        service.metaClass.getFileFromURL = { String url, String filename ->
            return fileInstance
        }

        mockGetPermanentURL()

        when: 'moveFilesToCDN method is called'
        assert uFileInstance.provider == CDNProvider.GOOGLE
        service.moveFilesToCDN([uFileInstance], CDNProvider.AMAZON, true)

        then: 'File would be made public'
        uFileInstance.type == UFileType.CDN_PUBLIC
        uFileInstance.provider == CDNProvider.AMAZON

        cleanup:
        fileInstance?.delete()
    }

    void "test moveFilesToCDN method for failure cases"() {
        given: 'An instance of UFile and File'
        UFile uFileInstance = UFile.build(type: UFileType.LOCAL)
        File fileInstance = getFileInstance('/tmp/test.txt')

        when: 'moveFilesToCDN method is called and file does not exist'
        service.metaClass.getFileFromURL = { String url, String filename ->
            return fileInstance
        }

        mockExistMethod(false)

        def failedUploadList = service.moveFilesToCDN([uFileInstance], CDNProvider.AMAZON)

        then: 'File would be moved successfully and method would return empty list for failed uploads'
        failedUploadList == []

        cleanup:
        fileInstance?.delete()
    }

    void "test moveToNewCDN method for various cases"() {
        given: 'Mocked method'
        service.metaClass.moveFilesToCDN = { List<UFile> uFileList, CDNProvider toCDNProvider ->
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

    void "test updateAllUFileCacheHeader for various cases"() {
        given: 'An instance of UFile and Mocked method'
        UFile uFileInstance = UFile.build()
        mockAuthenticateMethod()

        GroovyMock(UFile, global: true)
        UFile.withCriteria(_) >> { Closure closure ->
            assert closure != null
            new JsonBuilder() closure
            return uFileInstance
        }

        amazonCDNFileUploaderImplMock.updatePreviousFileMetaData(_, _, _, _) >> {
            return
        }

        when: 'updateAllUFileCacheHeader method is called and provider is not AMAZON'
        def result = service.updateAllUFileCacheHeader(CDNProvider.GOOGLE)

        then: 'Method returns null'
        result == null

        when: 'updateAllUFileCacheHeader method is called and method executes successfully'
        service.updateAllUFileCacheHeader()

        then: 'No exceptions are thrown'
        noExceptionThrown()
    }

    void "test getFileFromURL method to return a file"() {
        when: 'getFileFromURL method is called'
        File responseFile = service.getFileFromURL('http://causecode.com/test', 'test')

        then: 'Method returns a file'
        responseFile != null
    }

    void "test cloneFile method for various cases"() {
        given: 'An instance of UFile and File'
        UFile ufIleInstance = UFile.build(name: 'test-file-1')
        File fileInstance = getFileInstance('./temp/test.txt')

        and: 'Mocked method'
        service.metaClass.saveFile = {
            String group,
            def file,
            String customFileName = '',
            Object userInstance = null,
            Locale locale = null -> return ufIleInstance
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
        fileInstance?.delete()
    }

    void "test cloneFile method for LOCAL type file"() {
        given: 'An instance of UFile and File'
        UFile ufIleInstance = UFile.build(name: 'test-file-1', path: 'http://unittest.com')
        File fileInstance = getFileInstance('/tmp/test.txt')

        and: 'Mocked method'
        service.metaClass.saveFile = {
            String group, def file,
            String customFileName = '',
            Object userInstance = null,
            Locale locale = null ->
                return ufIleInstance
        }

        when: 'cloneFile method is called for valid parameters and UFile is not LOCAL type'
        ufIleInstance.type = UFileType.CDN_PUBLIC

        UrlValidator urlValidatorMock = GroovyMock(UrlValidator, global: true)
        new UrlValidator() >> urlValidatorMock
        urlValidatorMock.isValid(_) >> {
            return true
        }

        def result = service.cloneFile('testGoogle', ufIleInstance, 'test')

        then: 'The method returns a valid file'
        result.name == 'test-file-1'

        cleanup:
        fileInstance.delete()
    }

    void "test resolvePath for various cases"() {
        given: 'An instance of UFile'
        UFile uFileInstance = UFile.build()

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

    void "test fileForUFile method when UFile is public type"() {
        given: 'An instance of File and UFile'
        File fileInstance = getFileInstance('/tmp/test.txt')
        UFile uFileInstance = UFile.build()

        and: 'Mocked methods'
        service.metaClass.getFileFromURL = { String url, String filename ->
            return fileInstance
        }

        mockExistMethod(true)

        when: 'UFile is PUBLIC type and file exist'
        uFileInstance.path = System.getProperty('user.dir') + '/temp/test.txt'
        uFileInstance.type = UFileType.CDN_PUBLIC
        def result = service.fileForUFile(uFileInstance, null)

        then: 'Method should return a file and downloads for the file should increase'
        uFileInstance.downloads == 1
        result != null

        cleanup:
        fileInstance.delete()
    }

    void "test deleteFileForUFile method for various cases"() {
        given: 'A UFile instance'
        UFile uFileInstance = UFile.build(type: UFileType.LOCAL)
        File fileInstance = getFileInstance('/tmp/test.txt')

        and: 'Mocked method'
        mockFileDeleteMethod(false)

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

    void "test deleteFileForUFile method for PUBLIC file"() {
        given: 'A UFile instance'
        UFile uFileInstance = UFile.build(type: UFileType.CDN_PUBLIC)
        assert uFileInstance.type == UFileType.CDN_PUBLIC

        and: 'Mocked getProviderInstance method'
        1 * service.utilitiesService.getProviderInstance(_) >> { String provider ->
            throw new ProviderNotFoundException('Provider RACKSPACE not found.')
        } >> { String provider ->
            return amazonCDNFileUploaderImplMock
        }

        and: 'Mocked method'
        googleCDNFileUploaderImplMock.deleteFile(_, _) >> {
        }

        when: 'deleteFileForUFile method is called and class does not exist'
        uFileInstance.provider = CDNProvider.RACKSPACE
        service.deleteFileForUFile(uFileInstance)

        then: 'Method returns true and no exceptions are thrown'
        ProviderNotFoundException e = thrown()
        e.message == 'Provider RACKSPACE not found.'

        when: 'deleteFileForUFile method is called and method executes successfully'
        uFileInstance.provider = CDNProvider.GOOGLE
        mockGetProviderInstance('google')
        def response = service.deleteFileForUFile(uFileInstance)

        then: 'Method returns true and no exceptions are thrown'
        noExceptionThrown()
        response
    }

    // Note: Creating test file for testing delete method call.
    @SuppressWarnings(['JavaIoPackageAccess'])
    void "test deleteFileForUFile method for LOCAL file"() {
        given: 'A UFile and a File instance'
        UFile uFileInstance = UFile.build(type: UFileType.LOCAL, path: '/tmp/testDir/test.txt')
        new File('/tmp/testDir').mkdir()

        and: 'Mocked method'
        mockExistMethod(true)
        mockFileDeleteMethod(true)

        when: 'deleteFileForUFile method is called and method executes successfully'
        service.deleteFileForUFile(uFileInstance)

        then: 'No exceptions are thrown'
        noExceptionThrown()
    }

    void "test deleteFileForUFile method for LOCAL file when parent folder not empty"() {
        given: 'A UFile and a File instance'
        UFile uFileInstance = UFile.build(type: UFileType.LOCAL)

        and: 'Mocked method'
        mockExistMethod(true)
        mockFileDeleteMethod(true)

        when: 'deleteFileForUFile method is called and method executes successfully'
        service.deleteFileForUFile(uFileInstance)

        then: 'No exceptions are thrown'
        noExceptionThrown()
    }

    void "test deleteFile method for various cases"() {
        given: 'An instance of UFile'
        UFile uFileInstance = UFile.build(type: UFileType.LOCAL)
        UFile.build(type: UFileType.LOCAL)

        when: 'deleteFile method is called and File is not found'
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

    void "test renewTemporaryURL method when fileUploaderInstance is null"() {
        given: 'Mocked method'
        service.utilitiesService.getProviderInstance(_) >> { String name ->
            return null
        }

        when: 'renewTemporaryURL method is called'
        def result = service.renewTemporaryURL()

        then: 'Method returns null'
        result == null
    }

    void "test saveFile method for various cases"() {
        given: 'Instances of CommonsMultipartFile and StandardMultipartFile'
        File fileInstance = getFileInstance('/tmp/test.txt')

        DiskFileItem fileItem = getDiskFileItemInstance(fileInstance)
        CommonsMultipartFile commonsMultipartFileInstance = new CommonsMultipartFile(fileItem)

        MultipartFile standardMultipartFile = new StandardMultipartFile(Mock(Part), 'test.txt')

        and: 'Mocked methods'
        mockFileGroupConstructor('CDN')
        mockAuthenticateMethod()
        mockExistMethod(false)
        mockGetFileNameAndExtensions()
        mockUploadFileMethod(true)
        mockGetProviderInstance('google')
        5 * fileGroupMock.cdnProvider >> {
            return
        } >> {
            return CDNProvider.GOOGLE
        }

        when: 'saveFile is called and provider is not specified'
        mockGetFileNameAndExtensions()
        service.saveFile('testGoogle', commonsMultipartFileInstance, 'test')

        then: 'Method should throw StorageConfigurationException'
        StorageConfigurationException e = thrown()
        e.message == 'Provider not defined in the Config. Please define one.'

        when: 'saveFile method is hit'
        mockUploadFileMethod(true)
        def result = service.saveFile('testGoogle', commonsMultipartFileInstance, 'test')

        then: 'Method should return instance of UFile'
        result.fileGroup == 'testGoogle'
        result.type == UFileType.CDN_PUBLIC

        when: 'saveFile method is hit and file belongs to StandardMultiartFile'
        mockUploadFileMethod(true)
        result = service.saveFile('testGoogle', standardMultipartFile, 'test')

        then: 'Method should return instance of UFile'
        result.fileGroup == 'testGoogle'
        result.type == UFileType.CDN_PUBLIC

        when: 'saveFile method is hit without a file'
        result = service.saveFile('testGoogle', null, 'test')

        then: 'Method should return null'
        result == null

        cleanup:
        fileInstance.delete()
    }

    void "test saveFile when provider is LOCAL"() {
        given: 'File instance'
        File fileInstance = getFileInstance('/tmp/test.txt')

        DiskFileItem fileItem = getDiskFileItemInstance(fileInstance)
        CommonsMultipartFile commonsMultipartFileInstance = new CommonsMultipartFile(fileItem)

        and: 'Mocked methods'
        mockFileGroupConstructor('LOCAL')
        2 * fileGroupMock.getFileNameAndExtensions(_, _) >> {
            return [fileName: 'test.txt', fileExtension: 'txt', customFileName: 'unit-test', empty: false,
                    fileSize: 38L]
        } >> {
            return [fileName: null, fileExtension: 'txt', customFileName: 'unit-test', empty: false,
                    fileSize: 38L]
        }

        2 * fileGroupMock.getLocalSystemPath(_, _, _) >> './temp/newDir'

        when: 'saveFile method is called and file gets saved'
        def result = service.saveFile('testLocal', fileInstance, 'test')

        then: 'Method should return saved UFile instance'
        result.id != null

        when: 'saveFile method is called and error occurs while saving file'
        result = service.saveFile('testLocal', commonsMultipartFileInstance, 'test')

        then: 'File would not be saved'
        result.id == null

        cleanup:
        fileInstance.delete()
    }

    void "test moveFailedFilesToCDN for various cases"() {
        given: 'Instances of UFileMoveHistory'
        UFileMoveHistory uFileMoveHistoryInstance = UFileMoveHistory.build(fromCDN: CDNProvider.RACKSPACE,
                toCDN: CDNProvider.GOOGLE, status: MoveStatus.FAILURE, ufile: UFile.build())

        service.metaClass.moveFilesToCDN = {
            List<UFile> uFileList,
            CDNProvider toCDNProvider,
            boolean makePublic = false -> return
        }

        when: 'moveFailedFilesToCDN method is called and failedList containes no UFiles'
        service.moveFailedFilesToCDN()

        then: 'No exception is thrown'
        noExceptionThrown()

        when: 'moveFailedFilesToCDN method is called and failedList containes UFiles'
        GroovyMock(UFileMoveHistory, global: true)
        UFileMoveHistory.withCriteria(_) >> { Closure closure ->
            assert closure != null
            new JsonBuilder() closure
            return [uFileMoveHistoryInstance]
        }

        service.moveFailedFilesToCDN()

        then: 'No exception is thrown'
        noExceptionThrown()
    }

    void "test ufileById method for various cases"() {
        given: 'An instance of UFile'
        UFile uFileInstance = UFile.build(type: UFileType.LOCAL)
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
        FileNotFoundException e = thrown()
        e.message == 'File not found'
    }

    void "test fileForUFile method when file does not exist"() {
        given: 'An instance of File and UFile'
        File fileInstance = getFileInstance('/tmp/test.txt')
        UFile uFileInstance = UFile.build(type: UFileType.LOCAL)

        and: 'Mocked methods'
        service.metaClass.getFileFromURL = { String url, String filename ->
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

    void "test saveFile method when validation fails"() {
        given: 'An instance of File'
        File fileInstance = getFileInstance('/tmp/test.txt')

        and: 'Mocked method'
        fileGroupMock.scopeFileName(_, _, _, _) >> {
            throw new StorageConfigurationException('Container name not defined in the Config. Please define one.')
        }

        mockGetFileNameAndExtensions()
        mockFileGroupConstructor()
        fileGroupMock.groupConfig >> [storageTypes: 'CDN']

        when: 'saveFile methods is called and exception is thrown while modifying fileName'
        service.saveFile('testGoogle', fileInstance)

        then: 'StorageConfigurationException is thrown'
        StorageConfigurationException e = thrown()
        e.message == 'Container name not defined in the Config. Please define one.'

        when: 'saveFile method is called and FileSize is greater than permitted value'
        fileGroupMock.validateFileSize(_, _) >> {
            throw new StorageConfigurationException('File too big.')
        }

        service.saveFile('testGoogle', fileInstance)

        then: 'Method throws StorageConfigurationException'
        StorageConfigurationException exception = thrown()
        exception.message == 'File too big.'

        cleanup:
        fileInstance.delete()
    }

    void "test moveFilesToCDN method when exception occurres while getting file from URL"() {
        given: 'An instance of UFile and File'
        UFile uFileInstance = UFile.build()
        File fileInstance = getFileInstance('/tmp/test.txt')

        and: 'Mocked method'
        service.metaClass.getFileFromURL = { String url, String filename ->
            throw new IOException('Error getting file from URL')
        }

        mockGetProviderInstance('google')

        mockUploadFileMethod(true)

        when: 'moveFilesToCDN method is called'
        assert uFileInstance.provider == CDNProvider.GOOGLE
        service.moveFilesToCDN([uFileInstance], CDNProvider.AMAZON)

        then: 'File won\'t be moved'
        uFileInstance.provider == CDNProvider.GOOGLE

        cleanup:
        fileInstance.delete()
    }

    void "Test renewTemporaryURL method in FileUploaderService class for forceAll=false and provider=mongodb"() {
        given: 'an instances of UFile class'
        UFile uFileInstance = new UFile(dateUploaded: new Date(), downloads: 0, extension: 'png', name: 'abc',
                path: 'https://xyz/abc', size: 12345, fileGroup: 'image', expiresOn: new Date(),
                provider: CDNProvider.AMAZON, type: UFileType.CDN_PUBLIC).save(flush: true)

        assert UFile.count() == 1

        Holders.flatConfig['fileuploader.persistence.provider'] = 'mongodb'

        and: 'Mocked AmazonCDNFileUploaderImpl\'s methods'
        mockAuthenticateMethod()
        mockGetTemporaryURL()

        and: 'Mocked getProviderInstance method'
        service.utilitiesService.getProviderInstance(_) >> { String providerName ->
            return amazonCDNFileUploaderInstance
        }

        and: 'Mocked withCriteria and getContainerName method'
        GroovyMock(UFile, global: true)
        UFile.withCriteria(_) >> { Closure closure ->
            assert closure != null
            new JsonBuilder() closure

            return [uFileInstance]
        }

        UFile.containerName(_) >> Holders.flatConfig["fileuploader.groups.${uFileInstance.fileGroup}.container"]

        when: 'renewTemporaryURL method is called'
        service.renewTemporaryURL()
        String uFilePath = uFileInstance.path

        then: 'It should only change image path of uFileInstance'
        uFilePath != 'https://xyz/abc'

        when: 'renewTemporaryURL method is called'
        uFileInstance.path = 'https://xyz/abc'
        uFileInstance.save(flush: true)

        assert uFileInstance.path == 'https://xyz/abc'
        service.renewTemporaryURL(true)

        then: 'It should renew the image path of all the Instance'
        uFileInstance.path != 'https://xyz/abc'
    }

    void "test saveFile method when file with same content uploaded twice"() {
        given: 'A file instance'
        File fileInstance = getFileInstance('/tmp/test.txt')

        and: 'Mocked authenticate method'
        mockAuthenticateMethod()

        and: 'Mocked getFileNameAndExtensions'
        mockGetFileNameAndExtensions()

        and: 'Mocked uploadFile method'
        mockUploadFileMethod(true)

        and: 'Mocked file.Exists method'
        mockExistMethod(true)

        and: 'Mocked getProviderInstance method'
        service.utilitiesService.getProviderInstance(_) >> { String providerName ->
            providerName == 'GOOGLE' ? googleCDNFileUploaderImplMock : amazonCDNFileUploaderImplMock
        }

        and: 'Mocked FileGroup Instance'
        new FileGroup(_) >> fileGroupMock
        fileGroupMock.cdnProvider >> CDNProvider.GOOGLE
        fileGroupMock.groupConfig >> [storageTypes: 'CDN', checksum: [calculate: true, algorithm: Algorithm.SHA1]]

        and: 'The saveFile method has been already called once for given file'
        UFile savedUfileInstance = service.saveFile('testGoogle', fileInstance, 'test')

        when: 'saveFile method gets called again on the file with same content'
        UFile.metaClass.static.findByChecksumAndChecksumAlgorithm = { String val, String val2 -> return new UFile() }
        service.saveFile('testGoogle', fileInstance, 'test')

        then: 'DuplicateFileException must be thrown'
        Exception exception = thrown(DuplicateFileException)
        String message = "Checksum for file test.txt is ${savedUfileInstance.checksum} and that checksum refers to an" +
                " existing file ${new UFile()} on server"
        exception.message.equalsIgnoreCase(message)
    }

    void "test saveFile method with invalid Algorithm instance"() {
        given: 'A file instance'
        File fileInstance = getFileInstance('/tmp/test.txt')

        and: 'Mocked authenticate method'
        mockAuthenticateMethod()

        and: 'Mocked getFileNameAndExtensions'
        mockGetFileNameAndExtensions()

        and: 'Mocked uploadFile method'
        mockUploadFileMethod(true)

        and: 'Mocked file.Exists method'
        mockExistMethod(true)

        and: 'Mocked getProviderInstance method'
        service.utilitiesService.getProviderInstance(_) >> { String providerName ->
            providerName == 'GOOGLE' ? googleCDNFileUploaderImplMock : amazonCDNFileUploaderImplMock
        }

        and: 'Mocked FileGroup Instance'
        new FileGroup(_) >> fileGroupMock
        fileGroupMock.cdnProvider >> CDNProvider.GOOGLE

        and: 'Invalid algorithm instance supplied'
        fileGroupMock.groupConfig >> [storageTypes: 'CDN', checksum: [calculate: true, algorithm: 'ABCD']]

        when: 'The saveFile method has been already called once for given file'
        service.saveFile('testGoogle', fileInstance, 'test')

        then: 'IllegalArgumentException must be thrown'
        Exception exception = thrown(IllegalArgumentException)
        exception.message == "No enum constant ${Algorithm.canonicalName}.ABCD"
    }
}

