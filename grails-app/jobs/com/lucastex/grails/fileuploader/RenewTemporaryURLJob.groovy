package com.lucastex.grails.fileuploader

import grails.util.Environment

class RenewTemporaryURLJob {

    static triggers = {
        cron name: "RenewTempURLTrigger", cronExpression: "0 0 2 * * ? *"   // Once every twenty four hours at 2am
    }

    def fileUploaderService

    def execute() {
        log.info "Started executing RenewTemporaryURLJob.."
        fileUploaderService.renewTemporaryURL()
        log.info "Finished executing RenewTemporaryURLJob."
    }

}