package com.causecode.fileuploader

import com.causecode.fileuploader.cdn.google.GoogleCDNFileUploaderImpl
import com.causecode.fileuploader.util.renewer.DefaultUFileTemporaryUrlRenewer
import grails.test.mixin.TestFor
import spock.lang.Specification

/**
 * Unit tests for {@link DefaultUFileTemporaryUrlRenewer}
 *
 * @author Milan Savaliya
 * @since 3.1.1
 */
@TestFor(UFileTemporaryUrlRenewerService)
class DefaultUFileTemporaryUrlRenewerServiceSpec extends Specification {

    def setup() {
        UtilitiesService utilitiesService = Mock(UtilitiesService)
        service.utilitiesService = utilitiesService
    }

    void "test renewTemporaryURL method"() {
        setup: 'mock getProviderInstance'
        service.utilitiesService.getProviderInstance(_) >> { String name -> return new GoogleCDNFileUploaderImpl() }

        and: 'Mocked DefaultUFileTemporaryUrlRenewer'
        DefaultUFileTemporaryUrlRenewer renewer = Mock(DefaultUFileTemporaryUrlRenewer)
        renewer.renew() >> { /*do nothing*/ }
        GroovyMock(DefaultUFileTemporaryUrlRenewer, global:true)
        new DefaultUFileTemporaryUrlRenewer(_,_,_) >> renewer

        when:
        service.renewTemporaryURL(true)

        then:
        noExceptionThrown()
    }
}
