grails.project.class.dir = "target/classes"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir  = "target/test-reports"

grails.project.dependency.resolver = "maven"
grails.project.dependency.resolution = {

    inherits("global")

    log "warn"
    repositories {
        grailsPlugins()
        grailsCentral()
        mavenCentral()
    }
    dependencies {
        compile ("org.apache.jclouds.provider:cloudfiles-us:1.7.0", "org.apache.jclouds:jclouds-compute:1.7.0",
                "org.apache.jclouds.provider:aws-s3:1.7.0") {
                    excludes "jclouds-core"
                }
        compile "org.apache.jclouds:jclouds-core:1.7.2"
    }
    plugins {
        // Make sure to comment while packaging to allow mongo db to install & hibernate to uninstall in parent app.
        /*runtime (":hibernate:3.6.10.7") {
            export = false
        }*/
        build(":tomcat:7.0.50", ":release:3.0.1", ":rest-client-builder:1.0.3") {
            export = false
        }
    }

}