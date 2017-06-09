/*
 * Copyright (c) 2011-Present, CauseCode Technologies Pvt Ltd, India.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or
 * without modification, are not permitted.
 */
package com.causecode.fileuploader.embedded

import com.causecode.mongo.embeddable.EmbeddableDomain

/**
 * This class represents embedded instance of UFile class.
 */
class EmUFile implements EmbeddableDomain {

    Long instanceId
    int downloads

    Date expiresOn

    String extension
    String name
    String path

    static constraints = { 
        expiresOn nullable: true
    }

    EmUFile(Map params) {
        this.instanceId = params.instanceId
        this.downloads = params.downloads
        this.expiresOn = params.expiresOn
        this.extension = params.extension
        this.name = params.name
        this.path = params.path
    }
}
