/*
 * Copyright (c) 2011 - Present, CauseCode Technologies Pvt Ltd, India.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or
 * without modification, are not permitted.
 */
package com.causecode.fileuploader.util.checksum

import com.causecode.fileuploader.BaseTestSetup
import com.causecode.fileuploader.FileGroup
import com.causecode.fileuploader.UFile
import com.causecode.fileuploader.util.checksum.exceptions.UnRecognizedFileTypeException
import grails.buildtestdata.mixin.Build
import org.grails.plugins.testing.GrailsMockMultipartFile
import spock.lang.Specification
import spock.lang.Unroll

/**
 * This class contains unit test cases for ChecksumValidator Class
 * @author Milan Savaliya
 * @since 3.1.0
 */
@Build(UFile)
@SuppressWarnings(['JavaIoPackageAccess'])
class ChecksumValidatorSpec extends Specification implements BaseTestSetup {

    @Unroll
    void "test Constructor with #fileName"() {
        given: 'fileGroup instance with supplied fileName'
        FileGroup fileGroupInstance = new FileGroup('testFile')

        and: 'supplied checksum config'
        fileGroupInstance.groupConfig.checksum = checksum

        when: 'constructor is called'
        ChecksumValidator instance = new ChecksumValidator(fileGroupInstance)

        then: 'expect a valid instance'
        instance != null

        where: 'inputs are as below'
        checksum << [null, [calculate: true, algorithm: Algorithm.SHA1]]
    }

    void "test isCalculate method with valid fileGroup instance"() {
        given: 'fileGroup instance'
        FileGroup fileGroupInstance = new FileGroup('testLocal')

        and: 'mocked config object'
        fileGroupInstance.groupConfig.checksum = [calculate: true, algorithm: Algorithm.SHA1]

        and: 'ChecksumValidator instance'
        ChecksumValidator instance = new ChecksumValidator(fileGroupInstance)

        expect: 'shouldCalculateChecksum returns true'
        instance.shouldCalculateChecksum()
    }

    void "test getChecksum method with a file instance"() {
        given: 'a file instance'
        File fileInstance = new File('/tmp/testLocal.txt')
        fileInstance.createNewFile()
        fileInstance << 'Some dummy date in'
        fileInstance.deleteOnExit()

        and: 'fileGroup instance'
        String groupName = 'testLocal'
        FileGroup fileGroupInstance = new FileGroup(groupName)

        and: 'valid ChecksumConfig object'
        fileGroupInstance.groupConfig.checksum = [calculate: true, algorithm: Algorithm.SHA1]

        when: 'getChecksum method is called'
        ChecksumValidator instance = new ChecksumValidator(fileGroupInstance)
        String checksum = instance.getChecksum(fileInstance)

        then: 'expect that checksum is calcuated'
        checksum
    }

    void "test getChecksum method with MultipartFile instance"() {
        given: 'MultipartFile instance'
        String fileName = 'testOne'
        GrailsMockMultipartFile multipartFile = new GrailsMockMultipartFile(fileName, fileName,
                'text', [1, 2, 3, 4, 5] as byte[])

        and: 'fileGroup instance'
        String groupName = 'testLocal'
        FileGroup fileGroupInstance = new FileGroup(groupName)

        and: 'valid ChecksumConfig object'
        fileGroupInstance.groupConfig.checksum = [calculate: true, algorithm: Algorithm.SHA1]

        when: 'getChecksum method is called with fileGroupInstance'
        ChecksumValidator instance = new ChecksumValidator(fileGroupInstance)
        String checksum = instance.getChecksum(multipartFile)

        then: 'expect that checksum is calcuated'
        checksum
    }

    void "test getAlgorithm method"() {
        given: 'fileGroup instance'
        String groupName = 'testLocal'
        FileGroup fileGroupInstance = new FileGroup(groupName)

        and: 'valid ChecksumConfig object'
        fileGroupInstance.groupConfig.checksum = [calculate: true, algorithm: Algorithm.SHA1]

        and: 'valid ChecksumValidator instance'
        ChecksumValidator instance = new ChecksumValidator(fileGroupInstance)

        when: 'getAlgorithm method is called'
        String algorithm = instance.algorithm

        then: 'expect supplied algorithm instance'
        algorithm == Algorithm.SHA1.toString()
    }

    void "test getFileInputBeanForFile method"() {
        given: 'instance which is not a type of java.io.File or Spring\'s MultipartFileUpload class'
        Object dummyObject = new Object()

        and: 'fileGroup instance'
        FileGroup fileGroupInstance = new FileGroup('testLocal')

        and: 'valid checksumConfig object'
        fileGroupInstance.groupConfig.checksum = [calculate: true, algorithm: Algorithm.SHA1]

        when: 'getChecksum method is called with dummyObject'
        ChecksumValidator instance = new ChecksumValidator(fileGroupInstance)
        instance.getChecksum(dummyObject)

        then: 'expect UnRecognizedFileTypeException'
        Exception exception = thrown(UnRecognizedFileTypeException)
        exception.message == "${dummyObject.class.name} is not recognized for FileInputBean"
    }
}
