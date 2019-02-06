package com.causecode.fileuploader.util

import spock.lang.Specification

/**
 * This class contains unit test cases for Time class
 */
class TimeSpec extends Specification {

    void "Test variable values"() {
        expect: 'Following must be true'
        assert Time.SECOND == 1L
        assert Time.MINUTE == 60L
        assert Time.HOUR == 3600L
        assert Time.DAY == 86400L
    }
}
