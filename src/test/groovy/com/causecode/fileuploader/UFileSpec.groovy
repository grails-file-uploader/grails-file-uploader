/*
 * Copyright (c) 2016, CauseCode Technologies Pvt Ltd, India.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or
 * without modification, are not permitted.
 */
package com.causecode.fileuploader

import grails.buildtestdata.mixin.Build
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import grails.util.Environment
import spock.lang.Specification

/**
 * This class contains unit test cases for UFile class.
 */
@TestFor(UFile)
@Build(UFile)
@Mock([FileUploaderService])
class UFileSpec extends Specification implements BaseTestSetup {

    void "test isFileExists method for various cases"() {
        given: 'An instance of UFile'
        UFile ufileInstance = UFile.build()
        ufileInstance.path = System.getProperty('user.dir') + '/temp'

        when: 'isFileExists method is called and file exists'
        boolean result = ufileInstance.isFileExists()

        then: 'Method returns true'
        result

        when: 'isFileExists method is called and file does not exist'
        ufileInstance.path = '/noFileExistHere/'
        result = ufileInstance.isFileExists()

        then: 'Method returns false'
        !result
    }

    void "test canMoveToCdn method for various cases"() {
        given: 'An instance of UFile'
        UFile uFileInstance = UFile.build(type: UFileType.LOCAL)

        when: 'canMoveToCDN method is called and UFile type is LOCAL'
        boolean result = uFileInstance.canMoveToCDN()

        then: 'Method returns true'
        result

        when: 'canMoveToCDN method is called and UFile type is not LOCAL'
        uFileInstance.type = UFileType.CDN_PUBLIC
        result = uFileInstance.canMoveToCDN()

        then: 'Method returns false'
        !result
    }

    void "test searchLink method to get path"() {
        given: 'An instance of UFile'
        UFile uFileInstance = UFile.build()
        uFileInstance.type = UFileType.CDN_PUBLIC

        when: 'searchLink method is called and UFile is PUBLIC type'
        String result = uFileInstance.searchLink()

        then: 'Method returns the path specified in instance'
        result == uFileInstance.path
    }

    void "test afterDelete method"() {
        given: 'An instance of UFile'
        UFile uFileInstance = UFile.build()
        uFileInstance.type = UFileType.CDN_PUBLIC
        uFileInstance.provider = CDNProvider.RACKSPACE

        when: 'afterDelete method is called for this instance'
        uFileInstance.afterDelete()

        then: 'Method should throw exception'
        ProviderNotFoundException e = thrown()
        e.message == 'Provider RACKSPACE not found.'
    }

    void "test containerName method for various cases"() {
        given: 'Parameter variable'
        String containerName = null

        when: 'containerName method is called and containerName parameter has null value'
        def result = UFile.containerName(containerName)

        then: 'The method should return null'
        result == null

        when: 'Environment is set as Production'
        containerName = 'test'
        Environment.metaClass.static.getCurrent = { ->
            return Environment.PRODUCTION
        }
        result = UFile.containerName(containerName)

        then: 'Method returns containerName'
        result == 'test'
    }
}
