/*
 * Copyright (c) 2011, CauseCode Technologies Pvt Ltd, India.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or
 * without modification, are not permitted.
 */

import com.lucastex.grails.fileuploader.CDNProvider
import com.lucastex.grails.fileuploader.util.Time

fileuploader {
    AmazonKey = "RANDOM_KEY"
    AmazonSecret = "RANDOM_SECRET"
    persistence.provider = "mongodb"
    user {
        maxSize = 1024 * 1024 * 2 // 2 MB
        allowedExtensions = ["jpg","jpeg","gif","png"]
        storageTypes = "CDN"
        container = "causecode-1"
        provider = CDNProvider.AMAZON
        makePublic = true
    }
    image {
        maxSize = 1024 * 1024 * 2 // 2 MB
        allowedExtensions = ["jpg","jpeg","gif","png"]
        storageTypes = "CDN"
        container = "causecode-1"
        provider = CDNProvider.AMAZON
        expirationPeriod = Time.DAY * 365
    }
    profile {
        maxSize = 1024 * 1024 * 2 // 2 MB
        allowedExtensions = ["jpg","jpeg","gif","png"]
        storageTypes = "CDN"
        container = "causecode-1"
        provider = CDNProvider.AMAZON
        expirationPeriod = Time.DAY * 365
    }
}
