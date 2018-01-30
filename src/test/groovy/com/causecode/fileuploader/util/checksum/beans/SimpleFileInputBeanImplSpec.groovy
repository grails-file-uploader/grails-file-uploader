package com.causecode.fileuploader.util.checksum.beans

import groovy.mock.interceptor.MockFor
import org.springframework.web.multipart.MultipartFile
import spock.lang.Specification

/**
 * @author Milan Savaliya
 */
class SimpleFileInputBeanImplSpec extends Specification {

    private final String tempDirPath = '/tmp/'
    private FileInputBean fileInputBean
    private String fileName

    def setup() {
        fileName = (System.currentTimeMillis() as String) + ".txt"
        def file = new File(tempDirPath.concat(fileName))
        file.createNewFile()
        file.deleteOnExit()
        fileInputBean = new SimpleFileInputBeanImpl(file)
    }

    def cleanup() {
    }

    void "test constructor"() {
        when: 'given an null object'
        new SimpleFileInputBeanImpl(null)
        then: 'expect a IllegalArgumentException'
        thrown(IllegalArgumentException)

        when: 'given an not exists file instance'
        new SimpleFileInputBeanImpl(new File('/tmp/text.txt'))
        then: 'expect a FileNotFoundException'
        thrown(FileNotFoundException)
    }

    void 'test getName method'() {
        when: 'Proper file instance with name provided'
        String filename = 'somename.txt'
        File file = new File("/tmp/${filename}")
        file.createNewFile()
        FileInputBean fileInputBean = new SimpleFileInputBeanImpl(file)
        then: 'getname must return valid filename'
        fileInputBean.getName() == filename
    }

    void 'test getOriginalFilename method'() {
        expect: 'name of the fileInputBean'
        fileInputBean.getOriginalFilename() != null
    }

    void 'test getContentType method'() {
        expect: 'name of the fileInputBean'
        fileInputBean.getContentType() != null
    }

    void 'test isEmpty method'() {
        expect: 'name of the fileInputBean'
        fileInputBean.isEmpty()
    }

    void 'test getSize method'() {
        expect: 'name of the fileInputBean'
        fileInputBean.getSize() == 0
    }

    void 'test getBytes method'() {
        expect: 'name of the fileInputBean'
        fileInputBean.getBytes().length == 0
    }

    void 'test getInputStream method'() {
        expect: 'name of the fileInputBean'
        def inputStream = fileInputBean.getInputStream()
        inputStream.getBytes().length == 0
        inputStream.close()
    }

    void 'test isExists method'(){
        expect: 'True when File exists'
        fileInputBean.isExists() == true
    }
}