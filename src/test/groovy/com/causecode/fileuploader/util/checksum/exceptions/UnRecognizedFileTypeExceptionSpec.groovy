package com.causecode.fileuploader.util.checksum.exceptions

import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import spock.lang.Specification

/**
 * @author Milan Savaliya
 */
@TestMixin(GrailsUnitTestMixin)
class UnRecognizedFileTypeExceptionSpec extends Specification {

    void 'test exception constructor'() {
        given: 'an instance of UnRecognizedFileTypeException'
        String message = 'Some Message'
        def exception = new UnRecognizedFileTypeException(message)
        expect: 'valid UnRecognizedFileTypeException instance'
        exception != null
        exception.message == message
    }

}
