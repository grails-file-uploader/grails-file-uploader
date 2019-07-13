/*
 * Copyright (c) 2011-Present, CauseCode Technologies Pvt Ltd, India.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or
 * without modification, are not permitted.
 */
package com.causecode.fileuploader

import com.causecode.fileuploader.logger.ReplaceSlf4jLogger
import com.causecode.fileuploader.provider.ProviderService
import grails.buildtestdata.mixin.Build
import grails.test.mixin.TestFor
import grails.util.Environment
import org.junit.Rule
import org.slf4j.Logger
import spock.lang.Specification
import spock.lang.Unroll

/**
 * This class contains unit test cases for UFile class.
 */
@TestFor(UFile)
@Build(UFile)
class UFileSpec extends Specification implements BaseTestSetup {

    Logger logger = Mock(Logger)
    @Rule ReplaceSlf4jLogger replaceSlf4jLogger = new ReplaceSlf4jLogger(UFile, logger)

    def setup() {
        ProviderService providerServiceMock = Mock(ProviderService)
        FileUploaderService service = Mock(FileUploaderService)
        service.providerService = providerServiceMock
    }

    void "test isFileExists method for various cases"() {
        given: 'An instance of UFile'
        UFile ufileInstance = UFile.build()
        ufileInstance.path = '/tmp'

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

        and: 'Mocked fileUploaderService method call'
        FileUploaderService fileUploaderServiceMock = Mock(FileUploaderService)
        fileUploaderServiceMock.resolvePath(_) >> {
            uFileInstance.path
        }

        uFileInstance.fileUploaderService = fileUploaderServiceMock

        when: 'searchLink method is called and UFile is PUBLIC type'
        String result = uFileInstance.searchLink()

        then: 'Method returns the path specified in instance'
        result == uFileInstance.path
    }

    void "test afterDelete method"() {
        given: 'An instance of UFile'
        UFile uFileInstance = UFile.build(type: UFileType.CDN_PUBLIC, provider: CDNProvider.RACKSPACE)

        and: 'Mocked fileUploaderService method call'
        FileUploaderService fileUploaderServiceMock = Mock(FileUploaderService)
        fileUploaderServiceMock.deleteFileForUFile(_) >> {
            throw new ProviderNotFoundException('Provider RACKSPACE not found.')
        }

        uFileInstance.fileUploaderService = fileUploaderServiceMock

        and: 'getProvider throws ProviderNotFoundException'
        uFileInstance.fileUploaderService.providerService.getProviderInstance(_) >> { String name ->
            throw new ProviderNotFoundException('Provider RACKSPACE not found.')
        }

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
        GroovyMock(Environment, global: true)
        1 * Environment.current >> {
            return Environment.PRODUCTION
        }

        result = UFile.containerName(containerName)

        then: 'Method returns containerName'
        result == 'test'
    }

    @Unroll
    void "test afterDelete method call to check for environment before deleting actual file when env #condition"() {
        given: 'An instance of UFile'
        UFile uFile = UFile.build()
        uFile.fileUploaderService = Mock(FileUploaderService)

        and: 'Mocked Environment'
        Environment environmentMock = GroovyMock(Environment, global: true)
        Environment.current >> {
            environmentMock.name >> {
                return currentEnv
            }

            return environmentMock
        }

        when: 'afterDelete method is called and environments do not match'
        uFile.afterDelete()

        then: 'The deleteFileForUFile method does not get called'
        1 * logger.warn(warnMessage)

        where:
        condition      | currentEnv   | warnMessage
        'match'        | 'test'       | 'Deleting file from CDN...'
        'don\'t match' | 'production' | 'File was uploaded from a different environment. Not deleting the actual file.'
    }
}
