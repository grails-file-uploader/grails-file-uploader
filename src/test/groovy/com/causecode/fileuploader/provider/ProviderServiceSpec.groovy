/*
 * Copyright (c) 2011 - Present, CauseCode Technologies Pvt Ltd, India.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or
 * without modification, are not permitted.
 */
package com.causecode.fileuploader.provider

import com.causecode.fileuploader.ProviderNotFoundException
import grails.testing.services.ServiceUnitTest
import spock.lang.Specification

/**
 * This class contains unit test cases for {@link ProviderService}.
 *
 * @author Hardik Modha
 * @since 3.1.3
 */
class ProviderServiceSpec extends Specification implements ServiceUnitTest<ProviderService> {
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
