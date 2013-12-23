import com.lucastex.grails.fileuploader.FileUploaderSecurityService

class FileUploaderGrailsPlugin {
    // the plugin version
    def version = "2.0.13"
    // the version or versions of Grails the plugin is designed for
    def grailsVersion = "2.1 > *"
    // the other plugins this plugin depends on
    def dependsOn = ["hibernate":"2.1 > *"]
    // resources that are excluded from plugin packaging
    def pluginExcludes = [
            "grails-app/views/error.gsp"
    ]

	def loadAfter = ['hibernate']

    def author = "Federico Hofman"
    def authorEmail = "fhofman@gmail.com"
    def title = "File Uploader Grails Plugin"
    def description = '''\\
This plugin provides easy integration with your grails application
to handle file uploading with multiple configuration.

This is a heavily modified version with updates from visheshd and danieldbower"
'''

    // URL to the plugin's documentation
    def documentation = "http://grails.org/plugin/file-uploader"
	
	def doWithSpring = {
		/* A default security service that simply permits all.  Should we enable
		 * by default?  Thinking that we should force the user to implement it
		 * right now
		 */
		//fileUploaderSecurityService(FileUploaderSecurityService)
	}
}
