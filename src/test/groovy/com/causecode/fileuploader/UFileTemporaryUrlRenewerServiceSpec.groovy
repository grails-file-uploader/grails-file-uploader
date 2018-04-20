package com.causecode.fileuploader

import com.causecode.fileuploader.cdn.google.GoogleCDNFileUploaderImpl
import com.causecode.fileuploader.util.UFileTemporaryUrlRenewer
import grails.test.mixin.TestFor
import spock.lang.Specification

/**
 * Unit tests for {@link com.causecode.fileuploader.util.UFileTemporaryUrlRenewer}
 *
 * @author Milan Savaliya
 * @since 3.1.1
 */
@TestFor(UFileTemporaryUrlRenewerService)
class UFileTemporaryUrlRenewerServiceSpec extends Specification {

    def setup() {
        UtilitiesService utilitiesService = Mock(UtilitiesService)
        service.utilitiesService = utilitiesService
    }

    void "test renewTemporaryURL method"() {
        setup: 'mock getProviderInstance'
        service.utilitiesService.getProviderInstance(_) >> { String name -> return new GoogleCDNFileUploaderImpl() }

        and: 'Mocked UFileTemporaryUrlRenewer'
        UFileTemporaryUrlRenewer renewer = Mock(UFileTemporaryUrlRenewer)
        renewer.start() >> { /*do nothing*/ }
        GroovyMock(UFileTemporaryUrlRenewer, global:true)
        new UFileTemporaryUrlRenewer(_,_,_) >> renewer

        when:
        service.renewTemporaryURL(true)

        then:
        noExceptionThrown()
    }
}
