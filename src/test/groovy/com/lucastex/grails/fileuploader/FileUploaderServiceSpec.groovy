/*
 * Copyright (c) 2011, CauseCode Technologies Pvt Ltd, India.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or
 * without modification, are not permitted.
 */

package com.lucastex.grails.fileuploader

import com.lucastex.grails.fileuploader.cdn.amazon.AmazonCDNFileUploaderImpl
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import org.grails.plugins.codecs.HTMLCodec
import spock.lang.Specification

@TestFor(FileUploaderService)
@TestMixin(GrailsUnitTestMixin)
@Mock([UFile])
class FileUploaderServiceSpec extends Specification {

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

        and: "Mocked AmazonCDNFileUploaderImpl's getTemporaryURL method"
        AmazonCDNFileUploaderImpl.metaClass.authenticate = {
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
}