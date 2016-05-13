/*
 * Copyright (c) 2011, CauseCode Technologies Pvt Ltd, India.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or
 * without modification, are not permitted.
 */

package com.lucastex.grails.fileuploader

import grails.test.mixin.TestFor
import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import org.grails.plugins.codecs.HTMLCodec
import spock.lang.Specification

@TestFor(FileUploaderService)
@TestMixin(GrailsUnitTestMixin)
class FileUploaderServiceSpec extends Specification {

    void "test isPublicGroup for various file groups"() {
        mockCodec(HTMLCodec)

        expect: "Following conditions should pass"
        service.isPublicGroup("user") == true
        service.isPublicGroup("image") == false
        service.isPublicGroup("profile") == false
        service.isPublicGroup() == false
    }
}