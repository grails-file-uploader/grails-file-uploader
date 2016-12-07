/*
 * Copyright (c) 2016, CauseCode Technologies Pvt Ltd, India.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or
 * without modification, are not permitted.
 */
package com.causecode.fileuploader

import grails.converters.JSON
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import spock.lang.Specification

@TestFor(FileUploaderController)
@Mock([UFile])
class FileUploaderControllerSpec extends Specification implements BaseTestSetup {

    void "test list action to get UFile realted information"() {
        given: "An instance of UFile"
        UFile uFileInstance = getUFileInstance(1)

        when: "list action is hit"
        controller.params.id = uFileInstance.id
        def response = controller.list()

        then: "Server repsonds UFileInstanceList and its count"
        controller.response.status == 200
        response.UFileInstanceList[0].id == 1
        response.UFileInstanceList[0].fileGroup == 'testGoogle'
        response.UFileInstanceTotal == 1

        when: "Params contain query to fetch list and no matching results are found"
        controller.params.query = 'updatesList'
        response = controller.list()

        then: "Server repsonds empty UFileInstanceList and its count"
        controller.response.status == 200
        response.UFileInstanceList == []
        response.UFileInstanceTotal == 0
    }

    void "test download action for various cases"() {
        given: "An instance of UFile and File"
        UFile uFileInstance = getUFileInstance(1)
        File fileInstance = getFileInstance()

        and: "Mocked fileUploaderService methods"
        FileUploaderService fileUploaderService = Mock(FileUploaderService)
        fileUploaderService.ufileById(_, _) >> { uFileInstance }
        fileUploaderService.fileForUFile(_, _) >> { throw new FileNotFoundException() } >> { return fileInstance }
        controller.fileUploaderService = fileUploaderService

        when: "download action is hit and FileNotFoundException is thrown"
        def result = controller.download()

        then: "Server returns null"
        result == null

        when: "upload action is hit and no exceptions are thrown"
        controller.response.reset()

        controller.download()

        then: "Following response should be true"
        noExceptionThrown()
        controller.response.status == 200
        controller.response.contentType == 'application/octet-stream'

        cleanup:
        fileInstance.delete()
    }

    void  "test show action for various cases"() {
        given: "A fileInstance and a uFileInstance"
        File fileInstance = getFileInstance()
        UFile uFileInstance = getUFileInstance(1)

        when: "show method is called and UFile instance is not found"
        controller.params.id = 2
        def result = controller.show()

        then: "Server responds with 404 error"
        result == null
        controller.response.status == 404

        when: "File does not exist on the provided path"
        controller.response.reset()
        uFileInstance.path = '/noFilesExistHere'
        controller.params.id = uFileInstance.id
        controller.show()

        then: "Server responds with 404 error"
        controller.response.status == 404

        when: "show action is hit and no errors occur"
        controller.response.reset()
        uFileInstance.path = System.getProperty('user.dir') + "/temp/test.txt"
        controller.params.id = uFileInstance.id
        controller.show()

        then: "Server responds with success"
        controller.response.contentType == 'image/jpg'
        controller.response.status == 200

        cleanup:
        fileInstance.delete()
    }

    void "test moveToCloud action for various cases"() {
        given: "An instance ofUFile"
        UFile uFileInstance = getUFileInstance(1)
        controller.request.json = ([provider: "GOOGLE", ufileIds: "1"] as JSON).toString()

        and: "Mocked method"
        controller.fileUploaderService = [moveFilesToCDN: { List<UFile> uFileList, CDNProvider toCDNProvider,
                boolean makePublic = false ->
            return [uFileInstance]
        }] as FileUploaderService

        when: "moveToCloud action is called"
        controller.moveToCloud()

        then: "No exception is thrown"
        noExceptionThrown()
        controller.response.status == 200
    }
}