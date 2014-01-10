import org.grails.plugins.localupload.LocalUploadSupportService


class LocalUploadGrailsPlugin {
    // the plugin version
    def version = "3.0.5"
    // the version or versions of Grails the plugin is designed for
    def grailsVersion = "2.1 > *"
    // the other plugins this plugin depends on
    def dependsOn = ["hibernate":"2.1 > *"]
    // resources that are excluded from plugin packaging
    def pluginExcludes = [
            "grails-app/views/error.gsp"
    ]

	def loadAfter = ['hibernate']

    def author = "Daniel Bower"
    def authorEmail = "daniel.d.bower@gmail.com"
    def title = "Local Upload Grails Plugin"
    def description = '''\\
This plugin provides a local file system resource to store files.

This plugin is heavily based on Federico Hofman's File Uploader Grails Plugin
with updates from visheshd, and lakshmanveti.
'''
	def doWithSpring = {
		/* A default security service that simply permits all.  Should we enable
		 * by default?  Thinking that we should force the user to implement it
		 * right now
		 */
		//localUploadSecurityService(LocalUploadSecurityService)
		
		/*
		 * A no-op version of ILocalUploadSupport service that enables the 
		 * plugin to run in a basic "no integration with my domain model" mode 
		 */
		localUploadSupportService(LocalUploadSupportService)
	}
}
