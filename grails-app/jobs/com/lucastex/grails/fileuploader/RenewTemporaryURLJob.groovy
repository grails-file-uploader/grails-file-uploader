package com.lucastex.grails.fileuploader

import grails.util.Environment

class RenewTemporaryURLJob {

    static triggers = {
        cron name: "RenewTempURLTrigger", cronExpression: "0 0 2 * * ? *"   // Once every twenty four hours at 2am
    }

    def fileUploaderService
    def grailsEvents

    def execute() {
        log.info "Started executing RenewTemporaryURLJob.."
        fileUploaderService.renewTemporaryURL()

        // Trigger event to notity the installing app for any further app specific processing
        grailsEvents.event("file-uploader", "on-ufile-renewal")

        log.info "Finished executing RenewTemporaryURLJob."
    }

}
