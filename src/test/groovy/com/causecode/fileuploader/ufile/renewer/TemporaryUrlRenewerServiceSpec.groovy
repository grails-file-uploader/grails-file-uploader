/*
 * Copyright (c) 2011-Present, CauseCode Technologies Pvt Ltd, India.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or
 * without modification, are not permitted.
 */
package com.causecode.fileuploader.ufile.renewer

import com.causecode.fileuploader.cdn.google.GoogleCDNFileUploaderImpl
import com.causecode.fileuploader.provider.ProviderService
import com.causecode.fileuploader.ufile.TemporaryUrlRenewerService
import grails.test.mixin.TestFor
import spock.lang.Specification

/**
 * Unit tests for {@link DefaultTemporaryUrlRenewer}
 *
 * @author Milan Savaliya
 * @since 3.1.3
 */
@TestFor(TemporaryUrlRenewerService)
class TemporaryUrlRenewerServiceSpec extends Specification {

    def setup() {
        ProviderService providerServiceMock = Mock(ProviderService)
        service.providerService = providerServiceMock
    }

    void "test renewTemporaryURL method when no error is thrown"() {
        setup: 'mock getProviderInstance'
        service.providerService.getProviderInstance(_) >> { String name -> return new GoogleCDNFileUploaderImpl() }

        and: 'Mocked DefaultTemporaryUrlRenewer'
        DefaultTemporaryUrlRenewer renewer = Mock(DefaultTemporaryUrlRenewer)
        renewer.renew() >> { /*do nothing*/ }
        GroovyMock(DefaultTemporaryUrlRenewer, global:true)
        new DefaultTemporaryUrlRenewer(_, _, _) >> renewer

        when: 'renewTemporaryURL method is called'
        service.renewTemporaryURL(true)

        then: 'no exception should be thrown'
        noExceptionThrown()
    }

    void "test renewTemporaryURL method when exception is thrown"() {
        setup: 'mock getProviderInstance'
        service.providerService.getProviderInstance(_) >> { String name -> return new GoogleCDNFileUploaderImpl() }

        and: 'Mocked DefaultTemporaryUrlRenewer'
        GroovyMock(DefaultTemporaryUrlRenewer, global:true)
        new DefaultTemporaryUrlRenewer(_ , _ ,_) >> { throw new IllegalArgumentException() }

        when: 'renewTemporaryURL method is called'
        service.renewTemporaryURL()

        then: 'Method should propagate the IllegalArgumentException'
        thrown(IllegalArgumentException)
    }
}
