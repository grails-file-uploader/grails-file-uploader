/*
 * Copyright (c) 2016, CauseCode Technologies Pvt Ltd, India.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or
 * without modification, are not permitted.
 */
package com.lucastex.grails.fileuploader

/**
 * A domain class which will hold the data about a UFile move history.
 *
 * @author Rohit Pal
 * @since 2.4.4
 */
@SuppressWarnings(['GrailsDomainHasEquals'])
class UFileMoveHistory {

    static constraints = {
        ufile(nullable: false)
        status(nullable: false)
        fromCDN(nullable: false)
        toCDN(nullable: false)
    }

    static mapping = {
        moveCount defaultValue: '0'
    }

    UFile ufile
    int moveCount
    Date lastUpdated
    CDNProvider fromCDN
    CDNProvider toCDN
    MoveStatus status
    String details

    @Override
    String toString() {
        "[$moveCount][$status]"
    }
}

@SuppressWarnings(['GrailsDomainHasEquals'])
enum MoveStatus {
    FAILURE(0),
    SUCCESS(1)

    final int id
    MoveStatus(int id) {
        this.id = id
    }

    int getValue() {
        return id
    }

    @Override
    String toString() {
        "${this.name()}($id)"
    }
}
