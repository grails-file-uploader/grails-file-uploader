/*
 * Copyright (c) 2016, CauseCode Technologies Pvt Ltd, India.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or
 * without modification, are not permitted.
 */
package grails.file.uploader

/**
 * This class is used for defining plugin related settings.
 */
@SuppressWarnings(['EmptyMethod', 'UnusedMethodParameter'])
class GrailsFileUploaderGrailsPlugin {

    def version = '3.0.1'
    def grailsVersion = '3.1.4 > *'
    def groupId = 'com.cc.plugins'
    def pluginExcludes = [
        'grails-app/views/error.gsp',
        'src/templates'
    ]

    def author = 'Federico Hofman'
    def authorEmail = 'fhofman@gmail.com'
    def title = 'File Uploader Grails Plugin'
    def description = 'This plugin provides easy integration with your Grails application' +
            'to handle file uploading with multiple configuration.' +

            'This is a heavily modified version with updates from visheshd, danieldbower, SAgrawal14' +
            'This plugin also supports uploading files to CDN for Rackspace & Amazon'

    def documentation = 'https://github.com/causecode/grails-file-uploader'
    def organization = [ name: 'CauseCode Technologies Pvt. Ltd.', url: 'http://causecode.com' ]
    def scm = [ url: 'https://github.com/causecode/grails-file-uploader/issues' ]

    Closure doWithSpring() { { ->
            // TODO Implement runtime spring config (optional)
        }
    }

    void doWithDynamicMethods() {
        // TODO Implement registering dynamic methods to classes (optional)
    }

    void doWithApplicationContext() {
        // TODO Implement post initialization spring config (optional)
    }

    void onChange(Map<String, Object> event) {
        // TODO Implement code that is executed when any artefact that this plugin is
        // watching is modified and reloaded. The event contains: event.source,
        // event.application, event.manager, event.ctx, and event.plugin.
    }

    void onConfigChange(Map<String, Object> event) {
        // TODO Implement code that is executed when the project configuration changes.
        // The event is the same as for 'onChange'.
    }

    void onShutdown(Map<String, Object> event) {
        // TODO Implement code that is executed when the application shuts down (optional)
    }
}
