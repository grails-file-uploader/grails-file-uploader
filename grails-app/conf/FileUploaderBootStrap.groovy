import com.lucastex.grails.fileuploader.RackspaceCDNFileUploaderService

class FileUploaderBootStrap {

    def fileUploaderService

    RackspaceCDNFileUploaderService rackspaceCDNFileUploaderService

    def init = { servletContext ->
        log.debug "Fileuploader bootstap started executing."
        if(rackspaceCDNFileUploaderService.authenticate()) {
            rackspaceCDNFileUploaderService.listContainers()
        }

        log.debug "Fileuploader bootstap finished executing."
    }

    def destroy = {
        rackspaceCDNFileUploaderService.close()
        log.info "Fileuploader Bootstrap destroyed."
    }
}