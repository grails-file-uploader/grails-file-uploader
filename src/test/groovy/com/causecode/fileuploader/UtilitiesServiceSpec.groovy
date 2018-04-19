package com.causecode.fileuploader

import grails.test.mixin.TestFor
import spock.lang.Specification

/**
 * See the API for {@link grails.test.mixin.services.ServiceUnitTestMixin} for usage instructions
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
}
