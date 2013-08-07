package com.lucastex.grails.fileuploader

class FileUploaderController {

    def messageSource
    def fileUploaderService

    def upload(String upload, String overrideFileName){
        def file = request.getFile("file")

        UFile ufile

        try{
            ufile = fileUploaderService.saveFile(upload, file, overrideFileName, request.locale)
        } catch(FileUploaderServiceException e) {
            flash.message = e.message
            redirect controller: params.errorController, action: params.errorAction, id: params.id
            return
        }

        redirect controller: params.successController, action: params.successAction, params:[ufileId: ufile.id,
            id: params.id, successParams: params.successParams]
    }

    def download() {
        UFile ufile
        File file

        try{
            ufile = fileUploaderService.ufileById(params.id, request.locale)
            file = fileUploaderService.fileForUFile(ufile, request.locale)
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

        log.debug "Serving file id=[${ufile.id}], downloaded for the ${ufile.downloads} time, to ${request.remoteAddr}"

        response.setContentType("application/octet-stream")
        response.setHeader("Content-disposition", "${params.contentDisposition}; filename=${ufile.name}")
        response.outputStream << file.readBytes()
        return
    }

    def show(Long id) {
        UFile ufile = UFile.get(id)
        if (!ufile) {
            response.sendError(404)
            return
        }
        File file = new File(ufile.path)
        if(file.exists()) {
            response.setContentType("image/" + ufile.extension)
            response.setContentLength(file.size().toInteger())
            OutputStream out
            try {
                out = response.getOutputStream()
                out?.write(file.bytes)
            } finally {
                out?.close()
            }
        }
    }

    def deleteFile(Long id, String successController, String errorController) {
        if(fileUploaderService.deleteFile(id)){
            redirect controller: successController, action: params.successAction, params:(params.successParams)
        } else {
            redirect controller: errorController, action: params.errorAction, params:(params.errorParams)
        }
    }

}