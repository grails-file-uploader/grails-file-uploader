grails.project.class.dir = "target/classes"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir  = "target/test-reports"

grails.project.dependency.resolution = {

    inherits("global")

    log "warn"
    repositories {
        inherits true // Whether to inherit repository definitions from plugins
        mavenLocal()
        grailsPlugins()
        grailsHome()
        grailsCentral()
        mavenCentral()
    }
    dependencies {
        compile ("org.apache.jclouds.provider:cloudfiles-us:1.7.2", "org.apache.jclouds:jclouds-compute:1.7.2",
                "org.apache.jclouds.provider:aws-s3:1.7.2") {
            excludes "jclouds-core", "guice"
        }
        compile "org.apache.jclouds:jclouds-core:1.7.2"
    }
    plugins {
        runtime ":hibernate:$grailsVersion"
        build(":tomcat:$grailsVersion", ":release:2.2.0", ":rest-client-builder:1.0.2") {
            export = false
        }
    }

}