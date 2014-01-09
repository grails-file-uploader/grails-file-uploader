import com.lucastex.grails.fileuploader.CDNFileUploaderService
import com.lucastex.grails.fileuploader.cdn.amazon.AmazonCDNFileUploaderImpl;

class FileUploaderBootStrap {

    def fileUploaderService
    def grailsApplication

    CDNFileUploaderService CDNFileUploaderService

    def init = { servletContext ->
        log.debug "Fileuploader bootstap started executing."
        if(CDNFileUploaderService.authenticate()) {
            CDNFileUploaderService.listContainers()
        }

        log.debug "Fileuploader bootstap finished executing."
    }

    def destroy = {
        CDNFileUploaderService.close()
        log.info "Fileuploader Bootstrap destroyed."
    }

}