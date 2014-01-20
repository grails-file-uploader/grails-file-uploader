/*
 * Copyright (c) 2011, CauseCode Technologies Pvt Ltd, India.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or
 * without modification, are not permitted.
 */

package com.lucastex.grails.fileuploader

import grails.util.Environment

class RenewTemporaryURLJob {

    def startDelay = 3600000    // Wait for 1 hrs after application startup
    def cronExpression = "0 0 2 * * ? *"  // Once every twenty four hours at 2am

    def fileUploaderService

    def execute() {
        log.info "Started executing RenewTemporaryURLJob.."
        fileUploaderService.renewTemporaryURL()
        log.info "Finished executing RenewTemporaryURLJob."
    }

}