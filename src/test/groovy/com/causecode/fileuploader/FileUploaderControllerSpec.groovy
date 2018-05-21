/*
 * Copyright (c) 2011-Present, CauseCode Technologies Pvt Ltd, India.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or
 * without modification, are not permitted.
 */
package com.causecode.fileuploader

import com.causecode.fileuploader.ufile.TemporaryUrlRenewerService
import grails.buildtestdata.BuildDataTest
import grails.buildtestdata.mixin.Build
import grails.converters.JSON
import grails.testing.web.controllers.ControllerUnitTest
import grails.util.Environment
import spock.lang.Specification
import spock.lang.Unroll

/**
 * This is unit test file for FileUploaderController class.
 */
@Build(UFile)
class FileUploaderControllerSpec extends Specification implements BaseTestSetup, 
        ControllerUnitTest<FileUploaderController>, BuildDataTest {

    // Note: Not a database query. Calling list action in FileUploaderController.
    @SuppressWarnings(['GrailsMaxForListQueries'])
    void "test list action to get UFile related information"() {
        given: 'An instance of UFile'
        UFile uFileInstance = UFile.build(fileGroup: 'testGoogle')

        when: 'list action is hit'
        controller.params.id = uFileInstance.id
        def response = controller.list()

        then: 'Server repsonds UFileInstanceList and its count'
        controller.response.status == 200
        response.UFileInstanceList[0].id == 1
        response.UFileInstanceList[0].fileGroup == 'testGoogle'
        response.UFileInstanceTotal == 1

        when: 'Params contain query to fetch list and no matching results are found'
        controller.params.query = 'updatesList'
        response = controller.list()

        then: 'Server repsonds empty UFileInstanceList and its count'
        controller.response.status == 200
        response.UFileInstanceList == []
        response.UFileInstanceTotal == 0
    }

    void "test download action for various cases"() {
        given: 'An instance of UFile and File'
        UFile uFileInstance = UFile.build()
        File fileInstance = getFileInstance('/tmp/test.txt')

        and: 'Mocked fileUploaderService methods'
        FileUploaderService fileUploaderService = Mock(FileUploaderService)
        fileUploaderService.ufileById(_, _) >> { uFileInstance }
        fileUploaderService.fileForUFile(_, _) >> { throw new FileNotFoundException() } >> { return fileInstance }
        controller.fileUploaderService = fileUploaderService

        when: 'download action is hit and FileNotFoundException is thrown'
        def result = controller.download()

        then: 'Server returns null'
        result == null

        when: 'upload action is hit and no exceptions are thrown'
        controller.response.reset()

        controller.download()

        then: 'Following response should be true'
        noExceptionThrown()
        controller.response.status == 200
        controller.response.contentType == 'application/octet-stream'

        cleanup:
        fileInstance?.delete()
    }

    void  "test show action for various cases"() {
        given: 'A fileInstance and a uFileInstance'
        File fileInstance = getFileInstance('/tmp/test.txt')
        UFile uFileInstance = UFile.build()

        when: 'show method is called and UFile instance is not found'
        controller.params.id = 2
        def result = controller.show()

        then: 'Server responds with 404 error'
        result == null
        controller.response.status == 404

        when: 'File does not exist on the provided path'
        controller.response.reset()
        uFileInstance.path = '/noFilesExistHere'
        controller.params.id = uFileInstance.id
        controller.show()

        then: 'Server responds with 404 error'
        controller.response.status == 404

        when: 'show action is hit and no errors occur'
        controller.response.reset()
        uFileInstance.path = '/tmp/test.txt'
        controller.params.id = uFileInstance.id
        controller.show()

        then: 'Server responds with success'
        controller.response.contentType == 'image/extension'
        controller.response.status == 200

        cleanup:
        fileInstance?.delete()
    }

    void "test moveToCloud action for various cases"() {
        given: 'An instance ofUFile'
        UFile uFileInstance = UFile.build()
        controller.request.json = ([provider: 'GOOGLE', ufileIds: '1'] as JSON).toString()

        and: 'Mocked method'
        controller.fileUploaderService = [moveFilesToCDN: { List<UFile> uFileList, CDNProvider toCDNProvider,
                boolean makePublic = false ->
            return [uFileInstance]
        } ] as FileUploaderService

        when: 'moveToCloud action is called'
        controller.moveToCloud()

        then: 'No exception is thrown'
        noExceptionThrown()
        controller.response.status == 200
    }

    void "test renew action"() {
        given: 'Mocked TemporaryUrlRenewerService methods'
        TemporaryUrlRenewerService renewerService = Mock(TemporaryUrlRenewerService)
        2 * renewerService.renewTemporaryURL() >> { } >> {
            throw new ProviderNotFoundException('Provider missing.')
        }

        controller.temporaryUrlRenewerService = renewerService

        when: 'renew action is executed successfully'
        boolean result  = controller.renew()

        then: 'No exception is thrown and controller returns true'
        noExceptionThrown()
        result

        when: 'renew action is not executed successfully'
        result = controller.renew()

        then: 'result must be false'
        controller.response.status == 404
        !result
    }

    @Unroll
    void "test moveFilesToGoogleCDN action when status from server is #receivedStatus"() {
        given: 'Mocked fileUploaderService method call'
        FileUploaderService fileUploaderServiceMock = Mock(FileUploaderService)
        if (receivedStatus == 'someExceptionOccurred') {
            fileUploaderServiceMock.moveToNewCDN(_, _) >> { throw new StorageException('No space available.') }
        }

        fileUploaderServiceMock.moveToNewCDN(_, _) >> receivedStatus
        controller.fileUploaderService = fileUploaderServiceMock

        and: 'Mocked current environment'
        GroovyMock(Environment, global: true)
        Environment.current >> {
            return Environment.DEVELOPMENT
        }

        when: 'moveFilesToGoogleCDN endpoint is hit and service method call returns false'
        boolean result = controller.moveFilesToGoogleCDN()

        then: 'Server should return expected result value'
        noExceptionThrown()
        result == expectedResult

        where:
        receivedStatus << [false, 'someExceptionOccurred', true, false]
        expectedResult << [false, false, true, false]
    }

    @SuppressWarnings('JavaIoPackageAccess')
    void  "test show action error occur while serving image to response"() {
        given: 'A fileInstance and a uFileInstance'
        UFile uFileInstance = UFile.build()

        and: 'Mocked File Instance to throw an Exception when getBytes method is called'
        File file = GroovyMock(File, global: true)
        new File(_) >> file
        file.exists() >> true
        file.bytes >> {
            throw new IOException('Error serving image to response')
        }

        when: 'show method is called and UFile instance is not found'
        uFileInstance.path = System.getProperty('user.dir') + '/temp/test.txt'
        controller.params.id = uFileInstance.id
        def result = controller.show()

        then: 'Server responds with status 200'
        result == null
        controller.response.status == 200
    }
}
