package com.causecode.fileuploader.util.checksum.beans

import groovy.mock.interceptor.MockFor
import org.springframework.web.multipart.MultipartFile
import spock.lang.Specification

/**
 * @author Milan Savaliya
 */
class MultipartFileInputBeanImplSpec extends Specification {

    private final String tempDirPath = '/tmp/'

    def setup() {
    }

    def cleanup() {
    }

    void "test constructor"() {
        when: 'given an null object'
        new MultipartFileInputBeanImpl(null)
        then: 'expect a IllegalArgumentException'
        thrown(IllegalArgumentException)

        expect: 'a valid instance of SimpleFileInputBeanImpl'
        new MultipartFileInputBeanImpl(Mock(MultipartFile)) != null
    }

    void 'test getName method'() {
        given: 'mocked getName method of the fileInputBean'
        def fileName = 'testOne.txt'
        def multipartFile = new MockFor(MultipartFile)
        multipartFile.demand.getName { return fileName }
        def file = multipartFile.proxyInstance()
        def fileInputBean = new MultipartFileInputBeanImpl(file)

        expect: 'Valid fileName'
        fileInputBean.getName() == fileName
    }

    void 'test getOriginalFilename method'() {
        given: 'mocked getOriginalFilename method of the fileInputBean'
        def fileName = 'testOne.txt'
        def multipartFile = new MockFor(MultipartFile)
        multipartFile.demand.getOriginalFilename { return fileName }
        def file = multipartFile.proxyInstance()
        def fileInputBean = new MultipartFileInputBeanImpl(file)

        expect: 'Valid original fileName'
        fileInputBean.getOriginalFilename() == fileName
    }

    void 'test getContentType method'() {
        given: 'mocked getContentType method of the fileInputBean'
        def multipartFile = new MockFor(MultipartFile)
        multipartFile.demand.getContentType { return 'TEXT' }
        def file = multipartFile.proxyInstance()
        def fileInputBean = new MultipartFileInputBeanImpl(file)

        expect: 'Valid content type'
        fileInputBean.getContentType() == 'TEXT'
    }

    void 'test isEmpty method'() {
        given: 'mocked isEmpty method of the fileInputBean'
        def multipartFile = new MockFor(MultipartFile)
        multipartFile.demand.isEmpty { return false }
        def file = multipartFile.proxyInstance()
        def fileInputBean = new MultipartFileInputBeanImpl(file)

        expect: 'file is not empty'
        !fileInputBean.isEmpty()
    }

    void 'test getSize method'() {
        given: 'mocked getSize method of the fileInputBean'
        def multipartFile = new MockFor(MultipartFile)
        multipartFile.demand.getSize { return 123456 }
        def file = multipartFile.proxyInstance()
        def fileInputBean = new MultipartFileInputBeanImpl(file)

        expect: 'supplied size in return'
        fileInputBean.getSize() == 123456
    }

    void 'test getBytes method'() {
        given: 'mocked getBytes method of the fileInputBean'
        def multipartFile = new MockFor(MultipartFile)
        multipartFile.demand.getBytes { return ([1, 2, 3] as byte[]) }
        def file = multipartFile.proxyInstance()
        def fileInputBean = new MultipartFileInputBeanImpl(file)

        expect: 'total number of item is equal to supplied array'
        fileInputBean.getBytes().length == 3
    }

    void 'test getInputStream method'() {
        given: 'mocked getInputStream method of the fileInputBean'
        def multipartFile = new MockFor(MultipartFile)
        def inputStream = Mock(InputStream)
        multipartFile.demand.getInputStream { return inputStream }
        def file = multipartFile.proxyInstance()
        def fileInputBean = new MultipartFileInputBeanImpl(file)

        expect: 'supplied inputstream instance'
        fileInputBean.getInputStream() == inputStream
    }

    void 'test isExists method'(){
        given: 'mocked getInputStream method of the fileInputBean'
        def multipartFile = new MockFor(MultipartFile)
        multipartFile.demand.isExists { return true }
        def file = multipartFile.proxyInstance()
        def fileInputBean = new MultipartFileInputBeanImpl(file)

        expect: 'supplied inputstream instance'
        fileInputBean.isExists()
    }
}