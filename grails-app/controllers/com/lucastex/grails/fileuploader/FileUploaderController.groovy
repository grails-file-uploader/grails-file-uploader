package com.lucastex.grails.fileuploader

@SuppressWarnings("ReturnNullFromCatchBlock")
class FileUploaderController {

    def fileUploaderService

    def download() {
        File file
        UFile uFileInstance

        try{
            uFileInstance = fileUploaderService.ufileById(params.id, request.locale)
            file = fileUploaderService.fileForUFile(uFileInstance, request.locale)
        } catch(FileNotFoundException fnfe) {
            log.debug fnfe.message
            flash.message = fnfe.message
            redirect controller: params.errorController, action: params.errorAction
            return
        } catch(IOException ioe) {
            log.error ioe.message
            flash.message = ioe.message
            redirect controller: params.errorController, action: params.errorAction
            return
        }

        log.debug "Serving file id=[${uFileInstance.id}], downloaded for the ${uFileInstance.downloads} time, to ${request.remoteAddr}"

        response.setContentType("application/octet-stream")
        response.setHeader("Content-disposition", "${params.contentDisposition}; filename=${uFileInstance.name}")
        response.outputStream << file.readBytes()
    }

    def show() {
        def id = params.id  // Support both Long Id or Mongo's ObjectId
        UFile uFileInstance = UFile.get(id)
        if (!uFileInstance) {
            response.sendError(404)
            return
        }

        File file = new File(uFileInstance.path)
        if (file.exists()) {
            response.setContentType("image/" + uFileInstance.extension)
            response.setContentLength(file.size().toInteger())
            OutputStream out
            try {
                out = response.getOutputStream()
                out?.write(file.bytes)
            } catch(e) {
                log.error "Error serving image to response", e
            } finally {
                out?.close()
            }
        } else {
            log.warn "Missing file for UFile id [$id]."
            response.sendError(404)
        }
    }

    /**
     * Admin related actions.
     */
    def list(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        List UFileInstanceList = UFile.createCriteria().list(params) {
        }

        [UFileInstanceList: UFileInstanceList, UFileInstanceTotal: UFileInstanceList.totalCount]
    }

    def moveToCloud() {
        String container = grailsApplication.config.fileuploader.container
        List<Long> ufileIdList = params.list('ufileId')
        Set<UFile> validUFilesToMoveToCloud = []

        ufileIdList.each {
            UFile ufileInstance = UFile.get(it)
            if (ufileInstance?.canMoveToCDN() && ufileInstance.fileExists) validUFilesToMoveToCloud << ufileInstance
        }

        List<Long> failedUFileIdList = fileUploaderService.moveFileToCloud(validUFilesToMoveToCloud as List, container)

        int total = validUFilesToMoveToCloud.size()
        int totalMoved = validUFilesToMoveToCloud.size() - failedUFileIdList.size()

        String message = "$totalMoved/$total Files moved to cloud."
        if (failedUFileIdList) {
            message += " Id list of failed ufiles are: $failedUFileIdList"
        }
        flash.message = message

        render true
    }
}