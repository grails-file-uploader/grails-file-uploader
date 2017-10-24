/*
 * Copyright (c) 2016, CauseCode Technologies Pvt Ltd, India.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or
 * without modification, are not permitted.
 */
package com.causecode.fileuploader

import grails.buildtestdata.mixin.Build
import grails.converters.JSON
import grails.test.mixin.TestFor
import spock.lang.Specification
import spock.lang.Unroll

/**
 * This is unit test file for FileUploaderController class.
 */
@Build(UFile)
@TestFor(FileUploaderController)
class FileUploaderControllerSpec extends Specification implements BaseTestSetup {

    // Note: Not a database query. Calling list action in FileUploaderController.
    @SuppressWarnings(['GrailsMaxForListQueries'])
    void "test list action to get UFile realted information"() {
        given: 'An instance of UFile'
        UFile uFileInstance = UFile.build()

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
        File fileInstance = getFileInstance('./temp/test.txt')

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
        fileInstance.delete()
    }

    void  "test show action for various cases"() {
        given: 'A fileInstance and a uFileInstance'
        File fileInstance = getFileInstance('./temp/test.txt')
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
        uFileInstance.path = System.getProperty('user.dir') + '/temp/test.txt'
        controller.params.id = uFileInstance.id
        controller.show()

        then: 'Server responds with success'
        controller.response.contentType == 'image/extension'
        controller.response.status == 200

        cleanup:
        fileInstance.delete()
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
        given: 'Mocked fileUploaderService methods'
        FileUploaderService fileUploaderService = Mock(FileUploaderService)
        2 * fileUploaderService.renewTemporaryURL() >> { } >> {
            throw new ProviderNotFoundException('Provider missing.')
        }
        controller.fileUploaderService = fileUploaderService

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

    void "test moveFilesToGoogleCDN action"() {
        given: 'Mocked fileUploaderService method call'
        FileUploaderService fileUploaderService = Mock(FileUploaderService)
        3 * fileUploaderService.moveToNewCDN(_, _) >> {
            return false
        } >> {
            throw new StorageException('No space available.')
        } >> {
            return true
        }
        controller.fileUploaderService = fileUploaderService

        when: 'moveFilesToGoogleCDN endpoint is hit and service method call returns false'
        boolean result = controller.moveFilesToGoogleCDN()

        then: 'Server returns false'
        !result

        when: 'moveFilesToGoogleCDN endpoint is hit and service method throws Exception'
        result = controller.moveFilesToGoogleCDN()

        then: 'Server returns false'
        !result

        when: 'moveFilesToGoogleCDN endpoint is hit and files are moved successfully'
        result = controller.moveFilesToGoogleCDN()

        then: 'Server returns true'
        result
    }
}
