/*
 * Copyright (c) 2016, CauseCode Technologies Pvt Ltd, India.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or
 * without modification, are not permitted.
 */
package com.causecode.fileuploader

import grails.test.mixin.TestFor
import spock.lang.Specification
import spock.lang.Unroll

@TestFor(UFileMoveHistory)
class UFileMoveHistorySpec extends Specification implements BaseTestSetup {

    void "test toString method to return expected response"() {
        given: "An instance of UFileMoveHistory"
        UFileMoveHistory uFileMoveHistoryInstance = getUFileMoveHistoryInstance(1)

        when: "toString method is called"
        String result = uFileMoveHistoryInstance.toString()

        then: "Resulting value must match the method response"
        result == "[$uFileMoveHistoryInstance.moveCount][$uFileMoveHistoryInstance.status]".toString()
    }

    @Unroll
    void "test getValue method for various values"() {
        given: "An instance of MoveStatus"
        MoveStatus moveStatusInstance

        when: "getValue method is called"
        moveStatusInstance = val
        def result = moveStatusInstance.getValue()

        then: "The result should match the expected value"
        result == expValue

        where:
        val                | expValue
        MoveStatus.SUCCESS | 1
        MoveStatus.FAILURE | 0
    }
}