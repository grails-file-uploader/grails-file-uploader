package com.causecode.fileuploader.util.checksum.beans

import org.grails.plugins.testing.GrailsMockMultipartFile
import org.springframework.web.multipart.MultipartFile
import spock.lang.Specification

/**
 * Unit Test Class for MultipartFileInputBeanImplSpec class
 * @author Milan Savaliya
 * @since 3.1.0
 */

class MultipartFileInputBeanImplSpec extends Specification {

    private static final String FILE_NAME = 'text.txt'

    void "test constructor with valid and invalid multipart instances"() {
        when: 'given an null object as parameter'
        MultipartFileInputBeanImpl.newInstance(null)

        then: 'expect a IllegalArgumentException'
        Exception exception = thrown(IllegalArgumentException)
        exception.message == 'Multipart Instance can not be null'

        when: 'given a proper instance as a parameter'
        FileInputBean fileInputBean = MultipartFileInputBeanImpl.newInstance(Mock(MultipartFile))

        then: 'expect that proper instance created'
        fileInputBean
    }

    void "test getName method"() {
        given: 'mocked getName method of the fileInputBean'
        GrailsMockMultipartFile mockMultipartFile = new GrailsMockMultipartFile(FILE_NAME)
        FileInputBean fileInputBean = new MultipartFileInputBeanImpl(mockMultipartFile)

        expect: 'a Valid file name'
        fileInputBean.name == FILE_NAME
    }

    void "test getOriginalFilename method"() {
        given: 'mocked getOriginalFilename method of the fileInputBean'
        GrailsMockMultipartFile multipartFile = new GrailsMockMultipartFile(FILE_NAME,
                FILE_NAME,
                'TEXT', [1, 2, 3] as byte[])
        FileInputBean fileInputBean = new MultipartFileInputBeanImpl(multipartFile)

        expect: 'Valid original FILE_NAME'
        fileInputBean.originalFilename == FILE_NAME
    }

    void "test getContentType method"() {
        given: 'mocked getContentType method of the fileInputBean'
        String contentType = 'TEXT'
        GrailsMockMultipartFile multipartFile = new GrailsMockMultipartFile(FILE_NAME, '', contentType)
        FileInputBean fileInputBean = new MultipartFileInputBeanImpl(multipartFile)

        expect: 'Valid content type'
        fileInputBean.contentType == contentType
    }

    void "test isEmpty method"() {
        given: 'mocked isEmpty method of the fileInputBean'
        GrailsMockMultipartFile multipartFile = new GrailsMockMultipartFile(FILE_NAME, [1, 2, 3] as byte[])
        FileInputBean fileInputBean = new MultipartFileInputBeanImpl(multipartFile)

        expect: 'file is not empty'
        !fileInputBean.isEmpty()
    }

    void "test getSize method"() {
        given: 'mocked getSize method of the fileInputBean'
        GrailsMockMultipartFile multipartFile = new GrailsMockMultipartFile(FILE_NAME, [1, 2, 3] as byte[])
        FileInputBean fileInputBean = new MultipartFileInputBeanImpl(multipartFile)

        expect: 'total number of supplied items in return'
        fileInputBean.size == 3
    }

    void "test getBytes method"() {
        given: 'mocked getBytes method of the fileInputBean'
        GrailsMockMultipartFile multipartFile = new GrailsMockMultipartFile(FILE_NAME, [1, 2, 3] as byte[])
        FileInputBean fileInputBean = new MultipartFileInputBeanImpl(multipartFile)

        expect: 'total number of item is equal to total number of supplied array items'
        fileInputBean.bytes.length == 3
    }

    void "test getInputStream method"() {
        given: 'mocked getInputStream method of the fileInputBean'
        GrailsMockMultipartFile multipartFile = new GrailsMockMultipartFile(FILE_NAME, [1, 2, 3] as byte[])
        FileInputBean fileInputBean = new MultipartFileInputBeanImpl(multipartFile)

        expect: 'that supplied inputStream instance is equal to returned one'
        InputStream inputStream = fileInputBean.inputStream
        inputStream.available() == 3
        inputStream.close()
    }

    void "test isExists method"() {
        given: 'mocked getInputStream method of the fileInputBean'
        GrailsMockMultipartFile multipartFile = new GrailsMockMultipartFile(FILE_NAME)
        FileInputBean fileInputBean = new MultipartFileInputBeanImpl(multipartFile)

        expect: 'that file exists on server space'
        fileInputBean.isExists()
    }
}
