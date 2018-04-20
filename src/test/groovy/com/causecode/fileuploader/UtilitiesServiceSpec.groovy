package com.causecode.fileuploader

import grails.test.mixin.TestFor
import org.springframework.web.multipart.MultipartFile
import spock.lang.Specification

/**
 * Unit tests for {@link UtilitiesService}
 */
@TestFor(UtilitiesService)
class UtilitiesServiceSpec extends Specification {

    void "test getProviderInstance method"() {
        when: 'getProviderInstance method is called and class does not exist'
        service.getProviderInstance('test')

        then: 'Method should throw exception'
        ProviderNotFoundException e = thrown()
        e.message == 'Provider test not found.'

        when: 'getProviderInstance method is called and class exist'
        service.getProviderInstance('Amazon')

        then: 'No exception is thrown'
        noExceptionThrown()
    }

    void "test getNewTemporaryDirectoryPath method"() {
        when:
        service.getNewTemporaryDirectoryPath()
        then:
        noExceptionThrown()
    }

    void "test getTempFilePathForMultipartFile"() {
        when:
        service.getTempFilePathForMultipartFile('Foo', '.pdf')
        then:
        noExceptionThrown()
    }

    void "test moveFile"() {
        setup: 'Dummy Dir'
        File dir = new File('./dummyDir')
        dir.mkdir()
        dir.deleteOnExit()

        when: 'instance is of simpleFile'
        File f = new File('./test')
        f.deleteOnExit()
        service.moveFile(f, dir.getAbsolutePath())

        then:
        noExceptionThrown()

        when: 'instance is of multipart file'
        MultipartFile multipartFile = Mock(MultipartFile)
        multipartFile.transferTo(_) >> { File input -> }
        service.moveFile(multipartFile, dir.getAbsolutePath())

        then:
        noExceptionThrown()
    }
}
