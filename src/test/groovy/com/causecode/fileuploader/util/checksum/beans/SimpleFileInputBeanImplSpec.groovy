package com.causecode.fileuploader.util.checksum.beans

import spock.lang.Specification

/**
 * @author Milan Savaliya
 */
@SuppressWarnings(['JavaIoPackageAccess', 'UnusedObject'])
class SimpleFileInputBeanImplSpec extends Specification {

    private final String tempDirPath = '/tmp/'
    private FileInputBean fileInputBean
    private String fileName

    def setup() {
        fileName = (System.currentTimeMillis() as String) + '.txt'
        def file = new File(tempDirPath.concat(fileName))
        file.createNewFile()
        file.deleteOnExit()
        fileInputBean = new SimpleFileInputBeanImpl(file)
    }

    void 'test constructor'() {
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
        String filename = 'fooFile.txt'
        File file = new File("/tmp/${filename}")
        file.createNewFile()
        FileInputBean fileInputBean = new SimpleFileInputBeanImpl(file)
        then: 'getName must return valid filename'
        fileInputBean.name == filename
    }

    void 'test getOriginalFilename method'() {
        expect: 'name of the fileInputBean'
        fileInputBean.originalFilename != null
    }

    void 'test getContentType method'() {
        expect: 'name of the fileInputBean'
        fileInputBean.contentType != null
    }

    void 'test isEmpty method'() {
        expect: 'name of the fileInputBean'
        fileInputBean.isEmpty()
    }

    void 'test getSize method'() {
        expect: 'name of the fileInputBean'
        fileInputBean.size == 0
    }

    void 'test getBytes method'() {
        expect: 'name of the fileInputBean'
        fileInputBean.bytes.length == 0
    }

    void 'test getInputStream method'() {
        expect: 'name of the fileInputBean'
        def inputStream = fileInputBean.inputStream
        inputStream.bytes.length == 0
        inputStream.close()
    }

    void 'test isExists method'() {
        expect: 'True when File exists'
        fileInputBean.isExists()
    }
}
