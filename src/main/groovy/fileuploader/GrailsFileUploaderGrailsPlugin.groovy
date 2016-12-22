/*
 * Copyright (c) 2016, CauseCode Technologies Pvt Ltd, India.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or
 * without modification, are not permitted.
 */
package fileuploader

/**
 * This class is used for defining plugin related settings.
 */
class GrailsFileUploaderGrailsPlugin {

    def version = '3.0.1'
    def grailsVersion = '3.1.4 > *'
    def groupId = 'com.causecode.plugins'
    def pluginExcludes = [
        'grails-app/views/error.gsp',
        'src/templates',
        '**/UrlMappings*/**'
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

    /*
     * Note: Few default methods that were not required were removed. Please refer plugin docs if required.
     * Removed methods: doWithSpring, doWithApplicationContext, doWithDynamicMethods, onChange, onConfigChange
     * and onShutdown.
     */
}
