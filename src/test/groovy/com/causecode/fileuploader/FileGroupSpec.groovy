/*
 * Copyright (c) 2016, CauseCode Technologies Pvt Ltd, India.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or
 * without modification, are not permitted.
 */
package com.causecode.fileuploader

import grails.buildtestdata.mixin.Build
import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import org.apache.commons.fileupload.disk.DiskFileItem
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.web.multipart.commons.CommonsMultipartFile
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.multipart.support.StandardMultipartHttpServletRequest.StandardMultipartFile
import spock.lang.Specification

import javax.servlet.http.Part

/**
 * This is Unit test file for FileGroup class.
 */
@Build(UFile)
@TestMixin(GrailsUnitTestMixin)
class FileGroupSpec extends Specification implements BaseTestSetup {

    @SuppressWarnings(['JavaIoPackageAccess'])
    void "test getFileNameAndExtensions method to return File data"() {
        given: 'An instance of File'
        File fileInstance = new File('testLocal')
        FileGroup fileGroupInstance = new FileGroup('testLocal')

        when: 'getFileNameAndExtensions method is called'
        Map result = fileGroupInstance.getFileNameAndExtensions(fileInstance, 'testLocal.txt')

        then: 'Method returns a valid map'
        result.fileName == 'testLocal.txt'
        result.customFileName == 'testLocal.txt'
        result.empty == true
        result.fileSize == 0L

        cleanup:
        fileInstance.delete()
    }

    void "test allowedExtensions method when no configurations exist for the given group"() {
        given: 'An instance of FileGroup class'
        FileGroup fileGroupInstance = new FileGroup('test')

        when: 'allowedExtensions method is called and no configurations exist for given group'
        fileGroupInstance.allowedExtensions(null, null, 'test')

        then: 'Method throws StorageConfigurationException'
        StorageConfigurationException e = thrown()
        e.message == 'No config defined for group [test]. Please define one in your Config file.'
    }

    void "test allowedExtensions methods when wrong extension is passed"() {
        given: 'An instance of FileGroup class'
        FileGroup fileGroupInstance = new FileGroup('testGoogle')

        and: 'Mocked method'
        MessageSource testInstance = Mock(MessageSource)
        testInstance.getMessage(_, _, _) >> { 'Invalid extension' }
        fileGroupInstance.messageSource = testInstance

        when: 'allowedExtensions method is called and file has wrong extensions'
        fileGroupInstance.allowedExtensions([fileExtension: 'mkv'], null, 'test.mkv')

        then: 'Method throws StorageConfigurationException'
        StorageConfigurationException exception = thrown()
        exception.message == 'Invalid extension'
    }

    void "test getLocalSystemPath method to get localPath"() {
        given: 'An instance of FileGroup class'
        FileGroup fileGroupInstance = new FileGroup('testLocal')

        when: 'getLocalSystemPath method is called'
        String resultPath = fileGroupInstance.getLocalSystemPath('monthSubdirsuuid', null, 0L)

        then: 'Method returns path of file'
        resultPath.contains('./temp')
    }

    void "test scopeFileName when containerName does not exist"() {
        given: 'An instance of FileGroup class'
        UFile uFileInstance = UFile.build()
        FileGroup fileGroupInstance = new FileGroup('testGoogle')
        Map fileGroupMap = [fileName: 'test']

        when: 'scopeFileMethod is called and method executes successfully'
        fileGroupInstance.scopeFileName(uFileInstance, fileGroupMap, 'testGoogle', 0L)

        then: 'No exception is thrown'
        noExceptionThrown()
        fileGroupMap.fileName == 'testGoogle-0-test1-'

        when: 'scopeFileName method is called and container name does not exist'
        GroovyMock(UFile, global: true)
        UFile.containerName(_) >> {
            return null
        }

        fileGroupInstance.scopeFileName(null, null, null, 0L)

        then: 'Method throws StorageConfigurationException Exception'
        StorageConfigurationException e = thrown()
        e.message == 'Container name not defined in the Config. Please define one.'
    }

    void "test validateFileSize method when fileSize exceeds max value"() {
        given: 'An instance of FileGroup class'
        FileGroup fileGroupInstance = new FileGroup('testGoogle')
        Map fileGroupMap = [fileSize: 1024 * 1024 * 1024]
        Locale locale = LocaleContextHolder.locale

        and: 'Mocked method'
        MessageSource testInstance = Mock(MessageSource)
        testInstance.getMessage(_, _, _) >> { 'file too big' }
        fileGroupInstance.messageSource = testInstance

        when: 'validateFileSize method is called'
        fileGroupInstance.validateFileSize(fileGroupMap.fileSize, locale)

        then: 'Method should throw StorageConfigurationException'
        StorageConfigurationException e = thrown()
        e.message == 'file too big'
    }

    void "test getFileNameAndExtensions method when file belongs to MultipartFile"() {
        given: 'Instances of StandardMultipartFile, CommonsMultipartFile and FileGroup class'
        File fileInstance = getFileInstance('./temp/test.txt')
        DiskFileItem fileItem = getDiskFileItemInstance(fileInstance)
        CommonsMultipartFile commonsMultipartFileInstance = new CommonsMultipartFile(fileItem)

        MultipartFile standardMultipartFile = new StandardMultipartFile(Mock(Part), 'test.txt')

        FileGroup fileGroupInstance = new FileGroup('testLocal')

        when: 'getFileNameAndExtensions method is called for CommonsMultipartFile'
        Map result = fileGroupInstance.getFileNameAndExtensions(commonsMultipartFileInstance, 'testLocal.txt')

        then: 'Method returns a valid map'
        result.fileName == 'testLocal.txt'
        result.customFileName == 'testLocal.txt'
        result.empty == true
        result.fileSize == 0L

        when: 'getFileNameAndExtensions method is called for StandardMultipartFile'
        result = fileGroupInstance.getFileNameAndExtensions(standardMultipartFile, 'testLocal.txt')

        then: 'Method returns a valid map'
        result.fileName == 'testLocal.txt'
        result.customFileName == 'testLocal.txt'
        result.empty == true
        result.fileSize == 0L

        cleanup:
        fileInstance.delete()
    }

    void "test getCdnProvider method to return provider name"() {
        given: 'An instance of FileGroup class'
        FileGroup fileGroupInstance = new FileGroup('testGoogle')

        expect: 'Method to return correct CDn provider for this group'
        fileGroupInstance.cdnProvider == CDNProvider.GOOGLE
    }

    void "test getLocalSystemPath method to return localPath"() {
        given: 'An instance of FileGroup class'
        FileGroup fileGroupInstance = new FileGroup('testLocal')
        Map fileProperties = [fileName: 'test', fileExtension: 'txt']

        when: 'getLocalSystemPath method is called'
        String localPath = fileGroupInstance.getLocalSystemPath('', fileProperties, 0L)

        then: 'Method returns a valid local path'
        localPath == './temp/0/test.txt'
    }

    @SuppressWarnings('JavaIoPackageAccess')
    void "test getLocalSystemPath method when it fails to create a new directory"() {
        given: 'An instance of FileGroup class'
        FileGroup fileGroupInstance = new FileGroup('testLocal')
        Map fileProperties = [fileName: 'test', fileExtension: 'txt']

        and: 'Mocked mkdir method to return false'
        File file = GroovyMock(File, global: true)
        new File(_) >> file
        file.mkdirs() >> false

        when: 'getLocalSystemPath method is called'
        fileGroupInstance.getLocalSystemPath('', fileProperties, 0L)

        then: 'StorageConfigurationException will be thrown'
        StorageConfigurationException exception = thrown()
        exception.message == 'FileUploader plugin couldn\'t create directories: [./temp/0/]'
    }
}
