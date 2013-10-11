import com.lucastex.grails.fileuploader.CDNFileUploaderService

class FileUploaderBootStrap {

    CDNFileUploaderService CDNFileUploaderService

    def init = { servletContext ->
        log.debug "Fileuploader bootstap started executing."
        CDNFileUploaderService.authenticate()
        CDNFileUploaderService.listContainers()
        log.debug "Fileuploader bootstap finished executing."
    }

    def destroy = {
        CDNFileUploaderService.close()
        log.info "Fileuploader Bootstrap destroyed."
    }

}