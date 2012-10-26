class FileUploaderGrailsPlugin {
    // the plugin version
    def version = "1.2.1"
    // the version or versions of Grails the plugin is designed for
    def grailsVersion = "1.2-M1 > *"
    // the other plugins this plugin depends on
    def dependsOn = ["hibernate":"1.1 > *"]
    // resources that are excluded from plugin packaging
    def pluginExcludes = [
    ]

	def loadAfter = ['hibernate']

    // TODO Fill in these fields
    def author = "Federico Hofman"
    def authorEmail = "fhofman@gmail.com"
    def title = "File Uploader Grails Plugin"
    def description = '''\\
This plugin provides easy integration with your grails application
to handle file uploading with multiple configuration.
'''

    // URL to the plugin's documentation
    def documentation = "http://grails.org/plugin/file-uploader"

	def doWithSpring = {
		// TODO Implement runtime spring config (optional)
	}

    def doWithApplicationContext = { applicationContext ->
        // TODO Implement post initialization spring config (optional)
    }

    def doWithWebDescriptor = { xml ->
        // TODO Implement additions to web.xml (optional)
    }

    def doWithDynamicMethods = { ctx ->
        // TODO Implement registering dynamic methods to classes (optional)
    }

    def onChange = { event ->
        // TODO Implement code that is executed when any artefact that this plugin is
        // watching is modified and reloaded. The event contains: event.source,
        // event.application, event.manager, event.ctx, and event.plugin.
    }

    def onConfigChange = { event ->
        // TODO Implement code that is executed when the project configuration changes.
        // The event is the same as for 'onChange'.
    }
}
