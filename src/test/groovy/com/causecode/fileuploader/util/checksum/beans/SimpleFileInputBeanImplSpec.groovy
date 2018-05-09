/*
 * Copyright (c) 2011 - Present, CauseCode Technologies Pvt Ltd, India.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or
 * without modification, are not permitted.
 */
package com.causecode.fileuploader.util.checksum.beans

import spock.lang.Specification
import spock.lang.Unroll

/**
 * Unit Test Class for SimpleFileInputBeanImpl class
 * @author Milan Savaliya
 * @since 3.1.0
 */
@SuppressWarnings(['JavaIoPackageAccess'])
class SimpleFileInputBeanImplSpec extends Specification {

    private final String TEMP_DIR_PATH = '/tmp/'
    private FileInputBean fileInputBean
    private String fileName

    def setup() {
        fileName = (System.currentTimeMillis() as String) + '.txt'
        File file = new File(TEMP_DIR_PATH.concat(fileName))
        file.createNewFile()
        file.deleteOnExit()
        fileInputBean = new SimpleFileInputBeanImpl(file)
    }

    private static File getFileInstance(String filename) {
        File file = new File("/tmp/${filename}")
        file.createNewFile()
        file.deleteOnExit()
        file
    }

    @Unroll
    void "test constructor with file object as:- #fileObject"() {
        when: 'given an null object'
        SimpleFileInputBeanImpl.newInstance(fileObject)

        then: 'expect a IllegalArgumentException'
        Exception exception = thrown(exceptionToBeThrown)
        exception.message == exceptionMessage

        where: 'the given values are as following'
        fileObject           | exceptionToBeThrown      | exceptionMessage
        null                 | IllegalArgumentException | 'File instance can not be null'
        new File('text.txt') | FileNotFoundException    | 'File with name text.txt not found'
    }

    void "test getName method"() {
        given: 'Proper file instance'
        String filename = 'fooFile.txt'
        File file = getFileInstance(filename)

        and: 'Valid FileInputBean instance'
        FileInputBean fileInputBean = new SimpleFileInputBeanImpl(file)

        expect: 'that getName method returns a valid filename'
        fileInputBean.name == filename
    }

    void "test getOriginalFilename method"() {
        expect: 'that getOriginalFilename method returns a valid filename'
        fileInputBean.originalFilename
    }

    @Unroll
    void "test getContentType method wile file:- #simpleFileInputbean.filename"() {
        expect: ''
        simpleFileInputbean.contentType == output

        where: 'Given below inputs'
        simpleFileInputbean                                           | output
        new SimpleFileInputBeanImpl(getFileInstance('causecode.txt')) | 'txt'
        new SimpleFileInputBeanImpl(getFileInstance('causecode'))     | null
    }

    void "test isEmpty method"() {
        expect: 'that isEmpty method returns true'
        fileInputBean.isEmpty()
    }

    void "test getSize method"() {
        expect: 'that getSize method returns 0'
        fileInputBean.size == 0
    }

    void "test getBytes method"() {
        expect: 'that getBytes method returns 0'
        fileInputBean.bytes.length == 0
    }

    void "test getInputStream method"() {
        expect: 'that getInputStream method returns a valid inputStream object'
        InputStream inputStream = fileInputBean.inputStream
        inputStream.bytes.length == 0
        inputStream.close()
    }

    void "test isExists method"() {
        expect: 'True when File exists'
        fileInputBean.isExists()
    }
}
