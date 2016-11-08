/*
 * Copyright (c) 2016, CauseCode Technologies Pvt Ltd, India.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or
 * without modification, are not permitted.
 */
package com.lucastex.grails.fileuploader

import com.lucastex.grails.fileuploader.cdn.amazon.AmazonCDNFileUploaderImpl
import com.lucastex.grails.fileuploader.cdn.google.GoogleCDNFileUploaderImpl
import com.lucastex.grails.fileuploader.cdn.google.GoogleCredentials
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import org.grails.plugins.codecs.HTMLCodec
import spock.lang.Specification
import spock.lang.Unroll

@TestFor(FileUploaderService)
@Mock([UFile])
@TestMixin(GrailsUnitTestMixin)
class FileUploaderServiceSpec extends Specification {

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

    void "test isPublicGroup for various file groups"() {
        mockCodec(HTMLCodec)

        expect: "Following conditions should pass"
        service.isPublicGroup("user") == true
        service.isPublicGroup("image") == false
        service.isPublicGroup("profile") == false
        service.isPublicGroup() == false
    }

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
        AmazonCDNFileUploaderImpl.metaClass.authenticate = { ->
            return true
        }

        AmazonCDNFileUploaderImpl.metaClass.getTemporaryURL = { String containerName, String fileName, long expiration ->
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
    void "test saveFile for uploading files for CDNProvider #provider"() {
        given: "A file instance"
        File file = new File('test.txt')
        file.createNewFile()
        file << 'This is a test document.'

        and: "Mocked method"
        AmazonCDNFileUploaderImpl.metaClass.authenticate = {
            return true
        }

        when: "The saveFile method is called"
        UFile ufileInstancefile = service.saveFile(fileGroup, file, 'test')

        then: "UFile instance should be successfully saved"
        ufileInstancefile.id
        ufileInstancefile.provider == provider
        ufileInstancefile.extension == "txt"
        ufileInstancefile.fileGroup == fileGroup

        file.delete()

        where:
        fileGroup | provider
        "testAmazon" | CDNProvider.AMAZON
        "testGoogle" | CDNProvider.GOOGLE
    }

    void "test saveFile for uploading files with ProviderNotFoundException exception"() {
        given: "A file instance"
        File file = new File('test.txt')
        file.createNewFile()
        file << 'This is a test document.'

        // TODO: Use metaClass in the next release to mock the getProviderInstance method
        def mock = [getProviderInstance: { providerName ->
            throw new ProviderNotFoundException("Provider $providerName not found.")
        }] as FileUploaderService

        when: "The saveFile() method is called"
        UFile ufileInstancefile = mock.saveFile("testAmazon", file, 'test')

        then: "It should throw ProviderNotFoundException"
        ProviderNotFoundException e = thrown()
        e.message == "Provider AMAZON not found."

        cleanup:
        file.delete()
    }

    void "test saveFile method in FileUploaderService when file upload fails"() {
        given: "A file instance and mocked method 'uploadFile' of class GoogleCDNFileUploaderImpl"
        File file = new File('test.txt')
        file.createNewFile()
        file << 'This is a test document.'

        GoogleCDNFileUploaderImpl.metaClass.uploadFile = {
            String containerName, File fileToUpload, String fileName, boolean makePublic, long maxAge ->
                throw new UploadFailureException(fileName, containerName, new Throwable())
        }

        when: "The saveFile() method is called"
        UFile ufileInstancefile = service.saveFile("testGoogle", file, 'test')

        then: "It should throw UploadFailureException"
        UploadFailureException e = thrown()

        cleanup:
        file.delete()
    }
}