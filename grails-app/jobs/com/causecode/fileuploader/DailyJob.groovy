package com.causecode.fileuploader

import com.causecode.fileuploader.ufile.TemporaryUrlRenewerService
import grails.util.Holders

/**
 * A job which gets triggered at 2 am.
 */
class DailyJob {

    TemporaryUrlRenewerService temporaryUrlRenewerService

    FileUploaderService fileUploaderService
    def grailsEvents

    static triggers = {
        cron name: 'RenewTempURLTrigger', cronExpression: '0 0 2 * * ? *'   // Once every twenty four hours at 2am
    }

    def execute() {

        boolean renewJobDisabled = Holders.config.jobs.fileUploader.renewURLs.disable ?: false

        if (renewJobDisabled) {
            log.info 'Renew URLs DailyJob has been disabled by the installing application.'

            return
        }

        log.info 'Started executing DailyJob..'

        UFile.withNewSession {
            temporaryUrlRenewerService.renewTemporaryURL()
            fileUploaderService.moveFailedFilesToCDN()
        }

        log.info 'Finished executing DailyJob.'

        /*
         * Trigger event to notify the installing app for any further app specific processing.
         *
         * TODO This is not working. Need to investigate grails events.
         */
        // grailsEvents.event("file-uploader", "on-ufile-renewal")
    }
}
