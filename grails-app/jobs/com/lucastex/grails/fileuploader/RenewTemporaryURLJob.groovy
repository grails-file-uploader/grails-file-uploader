/*
 * Copyright (c) 2016, CauseCode Technologies Pvt Ltd, India.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or
 * without modification, are not permitted.
 */
package com.lucastex.grails.fileuploader

/**
 * A job which gets triggered at 2 am to renew temporary url.
 */
class RenewTemporaryURLJob {

    static triggers = {
        cron name: 'RenewTempURLTrigger', cronExpression: '0 0 2 * * ? *'   // Once every twenty four hours at 2am
    }

    def fileUploaderService
    def grailsEvents

    def execute() {
        log.info 'Started executing RenewTemporaryURLJob..'
        fileUploaderService.renewTemporaryURL()

        // Trigger event to notity the installing app for any further app specific processing
        grailsEvents.event('file-uploader', 'on-ufile-renewal')

        log.info 'Finished executing RenewTemporaryURLJob.'
    }

}
