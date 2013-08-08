package com.lucastex.grails.fileuploader

import org.springframework.dao.DataIntegrityViolationException

class FileUploaderController {

    def messageSource
    def fileUploaderService

    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

    def beforeInterceptor = [action: this.&validate, only: ["edit", "update", "delete"]]

    private UFile uFileInstance

    private validate() {
        uFileInstance = UFile.get(params.id)
        if(!uFileInstance) {
            flash.message = g.message(code: 'default.not.found.message', args: [message(code: 'UFile.label'), params.id])
            redirect(action: "list")
            return false
        }
        return true
    }

    def upload(String upload, String overrideFileName){
        def file = request.getFile("file")

        try{
            uFileInstance = fileUploaderService.saveFile(upload, file, overrideFileName, request.locale)
        } catch(FileUploaderServiceException e) {
            flash.message = e.message
            redirect controller: params.errorController, action: params.errorAction, id: params.id
            return
        }

        redirect controller: params.successController, action: params.successAction, params:[ufileId: uFileInstance.id,
            id: params.id, successParams: params.successParams]
    }

    def download() {
        File file

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
        return
    }

    def show(Long id) {
        uFileInstance = UFile.get(id)
        if (!uFileInstance) {
            response.sendError(404)
            return
        }
        File file = new File(uFileInstance.path)
        if(file.exists()) {
            response.setContentType("image/" + uFileInstance.extension)
            response.setContentLength(file.size().toInteger())
            OutputStream out
            try {
                out = response.getOutputStream()
                out?.write(file.bytes)
            } finally {
                out?.close()
            }
        } else {
            log.warn "Missing file for UFile id [$id]."
            response.sendError(404)
        }
    }

    def deleteFile(Long id, String successController, String errorController) {
        if(fileUploaderService.deleteFile(id)){
            redirect controller: successController, action: params.successAction, params:(params.successParams)
        } else {
            redirect controller: errorController, action: params.errorAction, params:(params.errorParams)
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

    def create() {
        [uFileInstance: new UFile(params)]
    }

    def save() {
        uFileInstance = new UFile(params)
        if (!uFileInstance.save(flush: true)) {
            render(view: "create", model: [uFileInstance: uFileInstance])
            return
        }

        flash.message = message(code: 'default.created.message', args: [message(code: 'UFile.label'), uFileInstance.id])
        redirect(action: "edit", id: uFileInstance.id)
    }

    def edit(Long id) {
        [uFileInstance: uFileInstance]
    }

    def update(Long id, Long version) {
        if(version != null) {
            if (uFileInstance.version > version) {
                uFileInstance.errors.rejectValue("version", "default.optimistic.locking.failure",
                        [message(code: 'UFile.label')] as Object[],
                        "Another user has updated this UFile while you were editing")
                render(view: "edit", model: [uFileInstance: uFileInstance])
                return
            }
        }

        uFileInstance.properties = params

        if (!uFileInstance.save(flush: true)) {
            render(view: "edit", model: [uFileInstance: uFileInstance])
            return
        }

        flash.message = message(code: 'default.updated.message', args: [message(code: 'UFile.label'), uFileInstance.id])
        redirect(action: "edit", id: uFileInstance.id)
    }

    def delete(Long id) {
        try {
            uFileInstance.delete(flush: true)
            flash.message = message(code: 'default.deleted.message', args: [message(code: 'UFile.label'), id])
            redirect(action: "list")
        } catch (DataIntegrityViolationException e) {
            flash.message = message(code: 'default.not.deleted.message', args: [message(code: 'UFile.label'), id])
            redirect(action: "edit", id: id)
        }
    }

}