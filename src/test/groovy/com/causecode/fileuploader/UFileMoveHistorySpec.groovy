/*
 * Copyright (c) 2016, CauseCode Technologies Pvt Ltd, India.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or
 * without modification, are not permitted.
 */
package com.causecode.fileuploader

import grails.testing.gorm.DomainUnitTest
import spock.lang.Specification
import spock.lang.Unroll

/**
 * This is unit test file for UFileMoveHistory class.
 */
class UFileMoveHistorySpec extends Specification implements DomainUnitTest<UFileMoveHistory>, BaseTestSetup {

    @Unroll
    void "test getValue method for various values"() {
        given: 'An instance of MoveStatus'
        MoveStatus moveStatusInstance

        when: 'getValue method is called'
        moveStatusInstance = val
        def result = moveStatusInstance.value

        then: 'The result should match the expected value'
        result == expValue

        where:
        val                | expValue
        MoveStatus.SUCCESS | 1
        MoveStatus.FAILURE | 0
    }
}
