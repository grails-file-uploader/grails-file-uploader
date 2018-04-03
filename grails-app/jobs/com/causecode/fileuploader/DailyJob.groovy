/*
 * Copyright (c) 2016, CauseCode Technologies Pvt Ltd, India.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or
 * without modification, are not permitted.
 */
package com.causecode.fileuploader

import grails.util.Holders

/**
 * A job which gets triggered at 2 am.
 */
class DailyJob {

    def fileUploaderService
    def grailsEvents

    static triggers = {
        cron name: 'RenewTempURLTrigger', cronExpression: '0 0 2 * * ? *'   // Once every twenty four hours at 2am
    }

    def execute() {
        log.info 'Started executing DailyJob..'

        boolean renewJobDisabled = Holders.config.jobs.fileUploader.renewURLs.disable ?: false

        if (!renewJobDisabled) {
            fileUploaderService.renewTemporaryURL()
            fileUploaderService.moveFailedFilesToCDN()
            log.info 'Finished executing DailyJob.'
        }

        /*
         * Trigger event to notity the installing app for any further app specific processing.
         *
         * TODO This is not working. Need to investigate grails events.
         */
        // grailsEvents.event("file-uploader", "on-ufile-renewal")
    }
}
