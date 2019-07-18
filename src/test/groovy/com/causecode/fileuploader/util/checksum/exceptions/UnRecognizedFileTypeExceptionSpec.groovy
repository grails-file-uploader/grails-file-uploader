package com.causecode.fileuploader.util.checksum.exceptions

import spock.lang.Specification

/**
 * Unit test class for UnRecognizedFileTypeExceptionSpec class
 * @author Milan Savaliya
 * @since 3.1.0
 */
class UnRecognizedFileTypeExceptionSpec extends Specification {

    void "test exception constructor"() {
        given: 'an instance of UnRecognizedFileTypeException'
        String message = 'Some Message'
        Exception exception = new UnRecognizedFileTypeException(message)

        expect: 'valid UnRecognizedFileTypeException instance'
        exception && exception.message == message
    }

}
