import com.lucastex.grails.fileuploader.FileUploaderService
import com.lucastex.grails.fileuploader.cdn.amazon.AmazonCDNFileUploaderImpl

class FileUploaderGrailsPlugin {

    def version = "2.3.1"
    def grailsVersion = "2.1 > *"
    def groupId = "com.cc.plugins"
    def pluginExcludes = [
        "grails-app/views/error.gsp",
        "src/templates"
    ]

    def author = "Federico Hofman"
    def authorEmail = "fhofman@gmail.com"
    def title = "File Uploader Grails Plugin"
    def description = '''
This plugin provides easy integration with your grails application
to handle file uploading with multiple configuration.

This is a heavily modified version with updates from visheshd, danieldbower, SAgrawal14"
This plugin now supports uploading files to CDN for rackspace & amazon.
'''
    def documentation = "https://github.com/causecode/grails-file-uploader"
    def organization = [ name: "CauseCode Technologies Pvt. Ltd.", url: "http://causecode.com" ]
    def scm = [ url: "https://github.com/causecode/grails-file-uploader/issues" ]

    def watchedResources = "file:./grails-app/services/*FileUploaderService.groovy"

    def doWithDynamicMethods = { ctx ->
        println "\nConfiguring file uploader plugin ..."
        addServiceMethods(ctx)
        println "... finished configuring file uploader plugin\n"
    }

    def onChange = { event ->
        if (event.source && application.isServiceClass(event.source)) {
            addServiceMethods(event.ctx)
        }
    }

    private void addServiceMethods(ctx) {
        println "\nAdding dynamic methods ..."
        println ""
        def grailsApplication = ctx.grailsApplication

        MetaClass metaClassInstance = FileUploaderService.class.metaClass

        def amazonKey = grailsApplication.config.fileuploader.AmazonKey
        def amazoneSecret = grailsApplication.config.fileuploader.AmazonSecret

        if(amazonKey instanceof ConfigObject || amazoneSecret instanceof ConfigObject) {
            log.info "No username or key configured for file uploader amazon CDN service."
        } else {
            if(!metaClassInstance.respondsTo(null, "getAmazonFileUploaderInstance")) {
                metaClassInstance.getAmazonFileUploaderInstance {
                    return new AmazonCDNFileUploaderImpl(amazonKey, amazoneSecret)
                }
            }
        }
    }

}