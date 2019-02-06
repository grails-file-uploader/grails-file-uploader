package com.causecode.fileuploader

import grails.boot.GrailsApp
import grails.boot.config.GrailsAutoConfiguration

/**
 * The Application class used By Spring Boot to start the application.
 */
class Application extends GrailsAutoConfiguration {
    static void main(String[] args) {
        GrailsApp.run(Application, args)
    }
}
