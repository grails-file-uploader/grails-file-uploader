/*
 * Copyright (c) 2011 - Present, CauseCode Technologies Pvt Ltd, India.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or
 * without modification, are not permitted.
 */
package com.causecode.fileuploader.util.checksum.exceptions

import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
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
