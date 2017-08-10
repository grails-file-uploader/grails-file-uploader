/*
 * Copyright (c) 2016, CauseCode Technologies Pvt Ltd, India.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or
 * without modification, are not permitted.
 */
package com.causecode.fileuploader

import org.springframework.security.access.annotation.Secured

/**
 * Provides default CRUD end point.
 */
@SuppressWarnings('ReturnNullFromCatchBlock')
class FileUploaderController {

    static namespace = 'v1'

    FileUploaderService fileUploaderService

    def download() {
        File file
        UFile uFileInstance

        try {
            uFileInstance = fileUploaderService.ufileById(params.id, request.locale)
            file = fileUploaderService.fileForUFile(uFileInstance, request.locale)
        } catch (FileNotFoundException | IOException e) {
            log.error e.message
            flash.message = e.message
            redirect controller: params.errorController, action: params.errorAction
            return
        }

        log.debug "Serving file id=[${uFileInstance.id}], downloaded for the ${uFileInstance.downloads} time," +
                "to ${request.remoteAddr}"

        response.setContentType('application/octet-stream')
        response.setHeader('Content-disposition', "${params.contentDisposition}; filename=${uFileInstance.name}")
        response.outputStream << file.readBytes()

        return
    }

    @SuppressWarnings(['JavaIoPackageAccess'])
    def show() {
        def id = params.id  // Support both Long Id and Mongo's ObjectId
        UFile uFileInstance = UFile.get(id)
        if (!uFileInstance) {
            response.sendError(404)
            return
        }

        File file = new File(uFileInstance.path)
        if (file.exists()) {
            response.setContentType('image/' + uFileInstance.extension)
            response.setContentLength(file.size().toInteger())
            OutputStream out
            try {
                out = response.outputStream
                out?.write(file.bytes)
            } catch (e) {
                log.error 'Error serving image to response', e
            } finally {
                out?.close()
            }
        } else {
            log.warn "Missing file for UFile id [$id]."
            response.sendError(404)
        }

        return
    }

    /**
     * Admin related actions.
     */
    def list(Integer max) {
        String query = params.query
        params.max = Math.min(max ?: 10, 100)
        List uFileInstanceList = UFile.createCriteria().list(params) {
            if (query) {
                List queries = query.tokenize(' ')
                queries.each {
                    ilike('name', "%${it}%")
                }
            }
        }

        [UFileInstanceList: uFileInstanceList, UFileInstanceTotal: uFileInstanceList.totalCount]
    }

    def moveToCloud() {
        params.putAll(request.JSON)
        params.max = params.max ?: 100

        CDNProvider toCDNProvider = params.provider

        Set<UFile> validUFilesToMoveToCloud = []
        List<UFile> uFileUploadFailureList = []

        List<UFile> uFileList = UFile.getAll(params.ufileIds)

        uFileList.each {
            if (it?.canMoveToCDN() && it.fileExists) {
                validUFilesToMoveToCloud << it
            }
        }

        uFileUploadFailureList = fileUploaderService.moveFilesToCDN(validUFilesToMoveToCloud as List, toCDNProvider)

        int total = validUFilesToMoveToCloud.size()
        int totalMoved = total - uFileUploadFailureList.size()

        String message = "$totalMoved/$total Files moved to cloud."
        if (uFileUploadFailureList) {
            message += "list of failed ufiles are: $uFileUploadFailureList"
        }
        flash.message = message

        render true
    }

    @Secured('ROLE_ADMIN')
    def renew() {
        try {
            fileUploaderService.renewTemporaryURL()
        } catch (ProviderNotFoundException e) {
            log.error e.message
            response.setStatus(404)

            return false
        }

        return true
    }
}
