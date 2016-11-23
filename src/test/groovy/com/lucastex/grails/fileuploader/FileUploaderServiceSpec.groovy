/*
 * Copyright (c) 2016, CauseCode Technologies Pvt Ltd, India.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or
 * without modification, are not permitted.
 */
package com.lucastex.grails.fileuploader

import com.lucastex.grails.fileuploader.cdn.BlobDetail
import com.lucastex.grails.fileuploader.cdn.amazon.AmazonCDNFileUploaderImpl
import com.lucastex.grails.fileuploader.cdn.google.GoogleCDNFileUploaderImpl
import com.lucastex.grails.fileuploader.cdn.google.GoogleCredentials
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import grails.test.runtime.DirtiesRuntime
import grails.util.Holders
import groovy.json.JsonBuilder
import org.apache.commons.fileupload.disk.DiskFileItem
import org.apache.commons.validator.UrlValidator
import org.grails.plugins.codecs.HTMLCodec
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.context.support.AbstractMessageSource
import org.springframework.web.multipart.commons.CommonsMultipartFile
import spock.lang.Specification
import spock.lang.Unroll
import java.text.MessageFormat

@TestFor(FileUploaderService)
@Mock([UFile, UFileMoveHistory])
@TestMixin(GrailsUnitTestMixin)
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
            return "http://fixedURL.com"
        }

        Closure uploadFile = { String containerName, File file, String fileName, boolean makePublic, long maxAge ->
            return true
        }

        AmazonCDNFileUploaderImpl.metaClass.getTemporaryURL = getTemporaryURL
        GoogleCDNFileUploaderImpl.metaClass.getTemporaryURL = getTemporaryURL

        AmazonCDNFileUploaderImpl.metaClass.uploadFile = uploadFile
        GoogleCDNFileUploaderImpl.metaClass.uploadFile = uploadFile
    }

    DiskFileItem getFileItem() {
        DiskFileItem fileItem = new DiskFileItem("file", "text/plain", false, fileInstance.getName(),
                (int) fileInstance.length() , fileInstance.getParentFile());
        fileItem.getOutputStream();
        return fileItem
    }

    void mockGetFileNameAndExtensions() {
        FileGroup.metaClass.getFileNameAndExtensions = { def file, String customFileName ->
            return [fileName: 'test.txt', fileExtension: 'txt', customFileName: 'unit-test', empty: false,
                    fileSize: 38L]
        }
    }

    void mockGetProviderMethod() {
        FileGroup.metaClass.getCdnProvider = { return null }
    }

    void mockExistMethod() {
        File.metaClass.exists = {
            return true
        }
    }

    void mockExistsMethodReturnFalse() {
        File.metaClass.exists = {
            return false
        }
    }

    boolean mockAuthenticateMethod() {
        AmazonCDNFileUploaderImpl.metaClass.authenticate = {
            return true
        }
    }

    void mockGetPermanentURL() {
        Closure getPermanentURL = { String containerName, String fileName ->
            return "http://fixedURL.com"
        }

        AmazonCDNFileUploaderImpl.metaClass.getPermanentURL = getPermanentURL
        GoogleCDNFileUploaderImpl.metaClass.getPermanentURL = getPermanentURL
    }

    @DirtiesRuntime
    void "test isPublicGroup for various file groups"() {
        mockCodec(HTMLCodec)

        expect: "Following conditions should pass"
        service.isPublicGroup("user") == true
        service.isPublicGroup("image") == false
        service.isPublicGroup("profile") == false
        service.isPublicGroup() == false
    }

    @DirtiesRuntime
    void "test moveFilesToCDN method for successfully moving a file"() {
        given: "An instance of UFile and File"
        UFile uFileInstance = getUFileInstance(1)
        File fileInstance = getFileInstance()
        uFileInstance.path = System.getProperty('user.dir') + "/temp/test.txt"

        and: "Mocked method"
        FileUploaderService.metaClass.getFileFromURL = { String url, String filename ->
            return fileInstance
        }

        when: "moveFilesToCDN method is called"
        assert uFileInstance.provider == CDNProvider.GOOGLE
        service.moveFilesToCDN([uFileInstance], CDNProvider.AMAZON)

        then: "File would be moved successfully"
        uFileInstance.provider == CDNProvider.AMAZON

        cleanup:
        fileInstance.delete()
    }

    @DirtiesRuntime
    void "Test renewTemporaryURL method in FileUploaderService class for forceAll=false"() {
        given: "a few instances of UFile class"
        UFile uFileInstance1 = new UFile(dateUploaded: new Date(), downloads: 0, extension: "png", name: "abc",
                path: "https://xyz/abc", size: 12345, fileGroup: "image", expiresOn: new Date() + 30,
                provider: CDNProvider.AMAZON, type: UFileType.CDN_PUBLIC).save(flush: true)
        UFile uFileInstance2 = new UFile(dateUploaded: new Date(), downloads: 0, extension: "png", name: "abc",
                path: "https://xyz/abc", size: 12345, fileGroup: "image", expiresOn: new Date() + 20,
                provider: CDNProvider.AMAZON, type: UFileType.CDN_PUBLIC).save(flush: true)
        UFile uFileInstance3 = new UFile(dateUploaded: new Date(), downloads: 0, extension: "png", name: "abc",
                path: "https://xyz/abc", size: 12345, fileGroup: "image", expiresOn: new Date() + 10,
                provider: CDNProvider.AMAZON, type: UFileType.CDN_PUBLIC).save(flush: true)
        UFile uFileInstance4 = new UFile(dateUploaded: new Date(), downloads: 0, extension: "png", name: "abc",
                path: "https://xyz/abc", size: 12345, fileGroup: "image", expiresOn: new Date(),
                provider: CDNProvider.AMAZON, type: UFileType.CDN_PUBLIC).save(flush: true)

        assert UFile.count() == 4

        and: "Mocked AmazonCDNFileUploaderImpl's methods"
        mockAuthenticateMethod()

        AmazonCDNFileUploaderImpl.metaClass.getTemporaryURL = { String containerName, String fileName, long expiration->
            return "http://fixedURL.com"
        }

        AmazonCDNFileUploaderImpl.metaClass.close = {
            return true
        }

        when: "renewTemporaryURL method is called"
        service.renewTemporaryURL()
        String uFilePath = uFileInstance4.path

        then: "It should only change image path of uFileInstance4"
        uFileInstance1.path == "https://xyz/abc"
        uFileInstance2.path == "https://xyz/abc"
        uFileInstance3.path == "https://xyz/abc"
        uFilePath == "http://fixedURL.com"

        when: "renewTemporaryURL method is called"
        uFileInstance4.path = "https://xyz/abc"
        uFileInstance4.save(flush: true)

        assert uFileInstance4.path == "https://xyz/abc"
        service.renewTemporaryURL(true)

        then: "It should renew the image path of all the Instance"
        uFileInstance1.path == "http://fixedURL.com"
        uFileInstance2.path == "http://fixedURL.com"
        uFileInstance3.path == "http://fixedURL.com"
        uFileInstance4.path == "http://fixedURL.com"
    }

    @Unroll
    @DirtiesRuntime
    void "test saveFile for uploading files for CDNProvider #provider"() {
        given: "A file instance"
        File file = getFileInstance()

        and: "Mocked method"
        mockAuthenticateMethod()

        when: "The saveFile method is called"
        UFile ufileInstancefile = service.saveFile(fileGroup, file, 'test')

        then: "UFile instance should be successfully saved"
        ufileInstancefile.id
        ufileInstancefile.provider == provider
        ufileInstancefile.extension == "txt"
        ufileInstancefile.container == "causecode-test"
        ufileInstancefile.fileGroup == fileGroup

        file.delete()

        where:
        fileGroup | provider
        "testAmazon" | CDNProvider.AMAZON
        "testGoogle" | CDNProvider.GOOGLE
    }

    @DirtiesRuntime
    void "test saveFile for uploading files with ProviderNotFoundException exception"() {
        given: "A file instance"
        File file = getFileInstance()

        // TODO: Use metaClass in the next release to mock the getProviderInstance method
        def mock = [getProviderInstance: { providerName ->
            throw new ProviderNotFoundException("Provider $providerName not found.")
        }] as FileUploaderService

        when: "The saveFile() method is called"
        mock.saveFile("testAmazon", file, 'test')

        then: "It should throw ProviderNotFoundException"
        ProviderNotFoundException e = thrown()
        e.message == "Provider AMAZON not found."

        cleanup:
        file.delete()
    }

    @DirtiesRuntime
    void "test saveFile method in FileUploaderService when file upload fails"() {
        given: "A file instance and mocked method 'uploadFile' of class GoogleCDNFileUploaderImpl"
        File file = getFileInstance()

        GoogleCDNFileUploaderImpl.metaClass.uploadFile = {
                String containerName, File fileToUpload, String fileName, boolean makePublic, long maxAge ->
            throw new UploadFailureException(fileName, containerName, new Throwable())
        }

        when: "The saveFile() method is called"
        service.saveFile("testGoogle", file, 'test')

        then: "It should throw UploadFailureException"
        UploadFailureException e = thrown()

        cleanup:
        file.delete()
    }

    @DirtiesRuntime
    void "test uploadFileToCloud method for successful execution"() {
        given: "A file instance and mocked method 'uploadFile' of class GoogleCDNFileUploaderImpl"
        File file = getFileInstance()
        FileGroup fileGroupInstance = new FileGroup('testGoogle')
        Holders.grailsApplication.config.fileuploader.groups.testGoogle.makePublic = true

        and: "Mocked methods"
        mockGetPermanentURL()

        when: "The uploadFileToCloud method is called"
        String resultPath = service.uploadFileToCloud([fileName: 'test', fileExtension: '.txt'],
                fileGroupInstance, file)

        then: "It should return path of uploaded file"
        resultPath == 'http://fixedURL.com'

        cleanup:
        file.delete()
    }

    @DirtiesRuntime
    void "test moveFilesToCDN method for making a file public while moving and error occurs"() {
        given: "An instance of UFile and File"
        UFile uFileInstance = getUFileInstance(1)
        uFileInstance.type = UFileType.CDN_PUBLIC
        File fileInstance = getFileInstance()
        uFileInstance.path = System.getProperty('user.dir') + "/temp/test.txt"

        and: "Mocked method"
        FileUploaderService.metaClass.getFileFromURL = { String url, String filename ->
            return fileInstance
        }

        when: "moveFilesToCDN method is called"
        assert uFileInstance.provider == CDNProvider.GOOGLE
        assert uFileInstance.type == UFileType.CDN_PUBLIC
        service.moveFilesToCDN([uFileInstance], CDNProvider.AMAZON, true)

        then: "File move history would contain failure status"
        UFileMoveHistory uFileMoveHistoryInstance = UFileMoveHistory.get(1)
        uFileMoveHistoryInstance.status == MoveStatus.FAILURE

        cleanup:
        fileInstance.delete()
    }

    @DirtiesRuntime
    void "test moveFilesToCDN method for making a file public while moving and no error occurs"() {
        given: "An instance of UFile and File"
        UFile uFileInstance = getUFileInstance(1)
        File fileInstance = getFileInstance()
        uFileInstance.path = System.getProperty('user.dir') + "/temp/test.txt"

        and: "Mocked method"
        FileUploaderService.metaClass.getFileFromURL = { String url, String filename ->
            return fileInstance
        }

        mockGetPermanentURL()

        when: "moveFilesToCDN method is called"
        assert uFileInstance.provider == CDNProvider.GOOGLE
        assert uFileInstance.type == UFileType.LOCAL
        service.moveFilesToCDN([uFileInstance], CDNProvider.AMAZON, true)

        then: "File would be made public"
        uFileInstance.type == UFileType.CDN_PUBLIC
        uFileInstance.provider == CDNProvider.AMAZON

        cleanup:
        fileInstance.delete()
    }

    @DirtiesRuntime
    void "test moveFilesToCDN method for failure cases"() {
        given: "An instance of UFile and File"
        UFile uFileInstance = getUFileInstance(1)
        File fileInstance = getFileInstance()

        when: "moveFilesToCDN method is called and file does not exist"
        FileUploaderService.metaClass.getFileFromURL = { String url, String filename ->
            return fileInstance
        }
        mockExistsMethodReturnFalse()

        def failedUploadList = service.moveFilesToCDN([uFileInstance], CDNProvider.AMAZON)

        then: "File would be moved successfully and method would return empty list for failed uploads"
        failedUploadList == []

        cleanup:
        fileInstance.delete()
    }

    @DirtiesRuntime
    void "test moveToNewCDN method for various cases"() {
        given: "Mocked method"
        FileUploaderService.metaClass.moveFilesToCDN = { List<UFile> uFileList, CDNProvider toCDNProvider ->
            return
        }

        when: "moveToNewCDN method is called and parameters are invalid"
        def result = service.moveToNewCDN(null, null)

        then: "method returs false"
        !result

        when: "Valid parameters are received"
        result = service.moveToNewCDN(CDNProvider.GOOGLE, 'dummy')

        then: "Method returns true"
        result
    }

    @DirtiesRuntime
    void "test updateAllUFileCacheHeader for various cases"() {
        given: "Mocked method"
        mockAuthenticateMethod()

        UFile.metaClass.static.withCriteria = { Closure closure ->
            assert closure != null
            new JsonBuilder() closure
            return getUFileInstance(1)
        }

        AmazonCDNFileUploaderImpl.metaClass.updatePreviousFileMetaData = { String containerName, String fileName,
                Boolean makePublic, long maxAge ->
            return }

        when: "updateAllUFileCacheHeader method is called and provider is not AMAZON"
        def result = service.updateAllUFileCacheHeader(CDNProvider.GOOGLE)

        then: "Method returns null"
        result == null

        when: "updateAllUFileCacheHeader method is called and method executes successfully"
        service.updateAllUFileCacheHeader()

        then: "No exceptions are thrown"
        noExceptionThrown()
    }

    @DirtiesRuntime
    void "test getFileFromURL method to return a file"() {
        when: "getFileFromURL method is called"
        File responseFile = service.getFileFromURL('http://causecode.com/test', 'test')

        then: "Method returns a file"
        responseFile != null
    }

    @DirtiesRuntime
    void "test cloneFile method for various cases"() {
        given: "An instance of UFile and File"
        UFile ufIleInstance = getUFileInstance(1)
        File fileInstance = getFileInstance()

        and: "Mocked method"
        FileUploaderService.metaClass.saveFile = { String group, def file, String customFileName = '',
                Object userInstance = null, Locale locale = null ->
            return ufIleInstance
        }

        when: "cloneFile method is called and uFileInstance is missing"
        def result = service.cloneFile('testGoogle', null)

        then: "Method returns null"
        result == null

        when: "cloneFile method is called for valid parameters and UFile is LOCAL type"
        ufIleInstance.type = UFileType.LOCAL
        result = service.cloneFile('testGoogle', ufIleInstance, 'test')

        then: "The method returns a valid file"
        result.name == 'test-file-1'

        cleanup:
        fileInstance.delete()
    }

    @DirtiesRuntime
    void "test cloneFile method for LOCAL type file"() {
        given: "An instance of UFile and File"
        UFile ufIleInstance = getUFileInstance(1)
        File fileInstance = getFileInstance()
        ufIleInstance.path = "http://causecode.com/test"

        and: "Mocked method"
        FileUploaderService.metaClass.saveFile = { String group, def file, String customFileName = '',
                Object userInstance = null, Locale locale = null ->
            return ufIleInstance
        }

        when: "cloneFile method is called for valid parameters and UFile is not LOCAL type"
        ufIleInstance.type = UFileType.CDN_PUBLIC

        UrlValidator.metaClass.isValid = { String value ->
            return true
        }

        def result = service.cloneFile('testGoogle', ufIleInstance, 'test')

        then: "The method returns a valid file"
        result.name == 'test-file-1'

        cleanup:
        fileInstance.delete()
    }

    @DirtiesRuntime
    void "test resolvePath for various cases"() {
        given: "An instance of UFile"
        UFile uFileInstance = getUFileInstance(1)

        when: "resolvePath method is caled and invalid params are passed"
        def result = service.resolvePath(null)

        then: "Method returns null"
        result == ''

        when: "ufileInstance type is LOCAL"
        uFileInstance.type = UFileType.LOCAL
        result = service.resolvePath(uFileInstance)

        then: "Following condition should be true"
        result == "/fileUploader/show/$uFileInstance.id"

        when: "UFile type is public"
        uFileInstance.type = UFileType.CDN_PUBLIC
        result = service.resolvePath(uFileInstance)

        then: "Method returns UFile instance path"
        result == uFileInstance.path
    }

    @DirtiesRuntime
    void "test fileForUFile method when UFile is public type"() {
        given: "An instance of File and UFile"
        File fileInstance = getFileInstance()
        UFile uFileInstance = getUFileInstance(1)

        and: "Mocked methods"
        FileUploaderService.metaClass.getFileFromURL = {String url, String filename ->
            return fileInstance
        }

        mockExistMethod()

        when: "UFile is PUBLIC type and file exist"
        uFileInstance.path = System.getProperty('user.dir') + '/temp/test.txt'
        uFileInstance.type = UFileType.CDN_PUBLIC
        def result = service.fileForUFile(uFileInstance, null)

        then: "Method should return a file and downloads for the file should increase"
        uFileInstance.downloads == 2
        result != null

        cleanup:
        fileInstance.delete()
    }

    @DirtiesRuntime
    void "test deleteFileForUFile method for various cases"() {
        given: "A UFile instance"
        UFile uFileInstance = getUFileInstance(1)
        File fileInstance = getFileInstance()
        uFileInstance.path = System.getProperty('user.dir') + '/temp/test.txt'

        and: "Mocked method"
        File.metaClass.delete = {
            return false
        }

        when: "deleteFileForUFile method is called and file does not exist"
        mockExistsMethodReturnFalse()
        def result = service.deleteFileForUFile(uFileInstance)

        then: "Method returns false"
        !result

        when: "File exists but deletion fails"
        mockExistMethod()
        uFileInstance.path = System.getProperty('user.dir') + '/temp/test.txt'
        service.deleteFileForUFile(uFileInstance)

        then: "File would not be deleted"
        fileInstance.exists()
    }

    @DirtiesRuntime
    void "test deleteFileForUFile method for PUBLIC file"() {
        given: "A UFile instance"
        UFile uFileInstance = getUFileInstance(1)
        uFileInstance.type = UFileType.CDN_PUBLIC

        and: "Mocked method"
        GoogleCDNFileUploaderImpl.metaClass.deleteFile = { String containerName, String fileName ->
        }

        when: "deleteFileForUFile method is called and method executes successfully"
        def response = service.deleteFileForUFile(uFileInstance)

        then: "Method returns true and no exceptions are thrown"
        noExceptionThrown()
        response
    }

    @DirtiesRuntime
    void "test deleteFileForUFile method for LOCAL file"() {
        given: "A UFile and a File instance"
        UFile uFileInstance = getUFileInstance(1)
        uFileInstance.path = System.getProperty('user.dir') + "/temp/testDir/test.txt"
        new File('./temp/testDir').mkdir()
        uFileInstance.type = UFileType.LOCAL

        and: "Mocked method"
        mockExistMethod()
        File.metaClass.delete = {
            return true
        }

        when: "deleteFileForUFile method is called and method executes successfully"
        service.deleteFileForUFile(uFileInstance)

        then: "No exceptions are thrown"
        noExceptionThrown()
    }

    @DirtiesRuntime
    void "test deleteFileForUFile method for LOCAL file when parent folder not empty"() {
        given: "A UFile and a File instance"
        UFile uFileInstance = getUFileInstance(1)
        uFileInstance.type = UFileType.LOCAL

        and: "Mocked method"
        mockExistMethod()
        File.metaClass.delete = {
            return true
        }

        when: "deleteFileForUFile method is called and method executes successfully"
        service.deleteFileForUFile(uFileInstance)

        then: "No exceptions are thrown"
        noExceptionThrown()
    }

    @DirtiesRuntime
    void "test deleteFile method for various cases"() {
        given: "An instance of UFile"
        UFile uFileInstance = getUFileInstance(1)
        UFile uFileInstance1 = getUFileInstance(2)

        when: "deleteFile method is called and File is nt found"
        def result = service.deleteFile(null)

        then: "Method returns false"
        !result

        when: "deleteFile method is called and file is deleted successfully"
        assert UFile.count() == 2
        result = service.deleteFile(uFileInstance.id)

        then: "Method returns true"
        UFile.count() == 1
        result
    }

    @DirtiesRuntime
    void "test renewTemporaryURL method when fileUploaderInstance is null"() {
        given: "Mocked method"
        FileUploaderService.metaClass.getProviderInstance = { String name ->
            return null
        }
        when: "renewTemporaryURL method is called"
        def result = service.renewTemporaryURL()

        then: "Method returns null"
        result == null
    }

    @DirtiesRuntime
    void "test getProviderInstance method class does not exist"() {
        when: "getProviderInstance method is called"
        service.getProviderInstance('test')

        then: "Method should throw exception"
        ProviderNotFoundException e = thrown()
        e.message == 'Provider test not found.'
    }

    // Fix this as per need.
    @DirtiesRuntime
    void "test saveFile method for various cases"() {
        given: "An instance of File"
        File fileInstance = getFileInstance()

        DiskFileItem fileItem = getFileItem()
        CommonsMultipartFile commonsMultipartFileInstance = new CommonsMultipartFile(fileItem);

        and: "Mocked methods"
        mockAuthenticateMethod()
        mockExistsMethodReturnFalse()

        when: "saveFile method is hit"
        def result = service.saveFile("testGoogle", commonsMultipartFileInstance, 'test')

        then: "Method should return null"
        result == null

        when: "saveFile is called and provider is not specified"
        mockGetFileNameAndExtensions()
        mockGetProviderMethod()

        service.saveFile("testGoogle", commonsMultipartFileInstance, 'test')

        then: "Method should throw StorageConfigurationException"
        StorageConfigurationException e = thrown()
        e.message == 'Provider not defined in the Config. Please define one.'

        cleanup:
        fileInstance.delete()
    }

    @DirtiesRuntime
    void "test saveFile when provider is LOCAL"() {
        given: "File instance"
        File fileInstance = getFileInstance()

        DiskFileItem fileItem = getFileItem()
        CommonsMultipartFile commonsMultipartFileInstance = new CommonsMultipartFile(fileItem);

        Holders.grailsApplication.config.fileuploader.groups.testGoogle.storageTypes = 'local'

        and: "Mocked methods"
        mockGetFileNameAndExtensions()

        when: "saveFile method is called and file gets saved"
        def result = service.saveFile("testGoogle", fileInstance, 'test')

        then: "Method should return saved UFile instance"
        result.id != null

        when: "saveFile method is called and error occures while saving file"
        FileGroup.metaClass.getFileNameAndExtensions = { def file, String customFileName ->
            return [fileName: null, fileExtension: 'txt', customFileName: 'unit-test', empty: false,
                    fileSize: 38L]
        }
        result = service.saveFile("testGoogle", commonsMultipartFileInstance, 'test')

        then: "File would not be saved"
        result.id == null

        cleanup:
        fileInstance.delete()
    }

    @DirtiesRuntime
    void "test moveFailedFilesToCDN for various cases"() {
        given: "Instances of UFileMoveHistory"
        UFileMoveHistory uFileMoveHistoryInstance = getUFileMoveHistoryInstance(1)

        FileUploaderService.metaClass.moveFilesToCDN = {List<UFile> uFileList, CDNProvider toCDNProvider,
            boolean makePublic = false ->
            return
        }

        when: "moveFailedFilesToCDN method is called and failedList containes no UFiles"
        service.moveFailedFilesToCDN()

        then: "No exception is thrown"
        noExceptionThrown()

        when: "moveFailedFilesToCDN method is called and failedList containes UFiles"
        UFileMoveHistory.metaClass.static.withCriteria = { Closure closure ->
            assert closure != null
            new JsonBuilder() closure
            return [uFileMoveHistoryInstance]
        }

        service.moveFailedFilesToCDN()

        then: "No exception is thrown"
        noExceptionThrown()
    }

    // TODO ------ mock getMessage method.
    @DirtiesRuntime
    void "test ufileById method for various cases"() {
        given: "An instance of UFile"
        UFile uFileInstance = getUFileInstance(1)
        Locale locale = LocaleContextHolder.getLocale()

        when: "ufileById method is called and UFile exists"
        def result = service.ufileById(1, locale)

        then: "Method returns ufile instance"
        result == uFileInstance
    }
}