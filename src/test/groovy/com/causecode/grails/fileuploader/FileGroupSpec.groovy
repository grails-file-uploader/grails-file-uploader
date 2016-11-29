/*
 * Copyright (c) 2016, CauseCode Technologies Pvt Ltd, India.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or
 * without modification, are not permitted.
 */
package com.causecode.grails.fileuploader

import grails.test.mixin.Mock
import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import grails.test.runtime.DirtiesRuntime
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder
import spock.lang.Specification

@Mock([UFile])
@TestMixin(GrailsUnitTestMixin)
class FileGroupSpec extends Specification  implements BaseTestSetup {

    void "test getFileNameAndExtensions method to return File data"() {
        given: "An instance of File"
        File fileInstance = new File('test')
        FileGroup fileGroupInstance = new FileGroup('testGoogle')

        when: "getFileNameAndExtensions method is called"
        Map result = fileGroupInstance.getFileNameAndExtensions(fileInstance, 'test.txt')

        then: "Method returns a valid map"
        result.fileName == 'test.txt'
        result.customFileName == 'test.txt'
        result.isFileEmpty == true
        result.fileSize == 0L

        cleanup:
        fileInstance.delete()
    }

    void "test allowedExtensions methods for failure cases"() {
        given: "An instance of FileGroup class"
        FileGroup fileGroupInstance = new FileGroup('test')

        when: "allowedExtensions method is called and no configurations exist for given group"
        fileGroupInstance.allowedExtensions(null, null, 'test')

        then: "Method throws StorageConfigurationException"
        StorageConfigurationException e = thrown()
        e.message == "No config defined for group [test]. Please define one in your Config file."
    }

    void "test allowedExtensions methods when wrong extension is passed"() {
        given: "An instance of FileGroup class"
        FileGroup fileGroupInstance = new FileGroup('testGoogle')

        and: 'Mocked method'
        MessageSource testInstance = Mock(MessageSource)
        testInstance.getMessage(_, _, _) >> { 'Invalid extension'}
        fileGroupInstance.messageSource = testInstance

        when: "allowedExtensions method is called and file has wrong extensions"
        fileGroupInstance.allowedExtensions([fileExtension: 'mkv'], null, 'test.mkv')

        then: "Method throws StorageConfigurationException"
        StorageConfigurationException exception =thrown()
        exception.message == 'Invalid extension'
    }

    void "test getLocalSystemPath method to get localPath"() {
        given: "An instance of FileGroup class"
        FileGroup fileGroupInstance = new FileGroup('testGoogle')

        when: "getLocalSystemPath method is called"
        String resultPath = fileGroupInstance.getLocalSystemPath('monthSubdirs uuid', null, 0L)

        then: "Method returns path of file"
        resultPath.contains('./temp')
    }

    @DirtiesRuntime
    void "test scopeFileName when containerName does not exist"() {
        given: "An instance of FileGroup class"
        FileGroup fileGroupInstance = new FileGroup('testGoogle')
        UFile uFileInstance = getUFileInstance(1)
        Map fileGroupMap = [fileName: 'test']

        when: "scopeFileMethod is called and method executes successfully"
        fileGroupInstance.scopeFileName(uFileInstance, fileGroupMap, 'testGoogle', 0L)

        then: "No exception is thrown"
        noExceptionThrown()
        fileGroupMap.fileName == 'testGoogle-0-test1-'

        when: "scopeFileName method is called and container name does not exist"
        UFile.metaClass.static.containerName = {String containerName ->
            return null
        }
        fileGroupInstance.scopeFileName(null, null, null, 0L)

        then: "Method throws StorageConfigurationException Exception"
        StorageConfigurationException e = thrown()
        e.message == 'Container name not defined in the Config. Please define one.'
    }

    void "test validateFileSize method when fileSize exceeds max value"() {
        given: "An instance of FileGroup class"
        FileGroup fileGroupInstance = new FileGroup('testGoogle')
        Map fileGroupMap = [fileSize: 1024 * 1024 * 1024]
        Locale locale = LocaleContextHolder.getLocale()

        and: "Mocked method"
        MessageSource testInstance = Mock(MessageSource)
        testInstance.getMessage(_, _, _) >> { 'file too big'}
        fileGroupInstance.messageSource = testInstance

        when: "validateFileSize method is called"
        fileGroupInstance.validateFileSize(fileGroupMap, locale)

        then: "Method should throw StorageConfigurationException"
        StorageConfigurationException e = thrown()
        e.message == 'file too big'
    }
}