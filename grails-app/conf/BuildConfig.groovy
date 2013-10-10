grails.project.class.dir = "target/classes"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir  = "target/test-reports"

grails.project.dependency.resolution = {

    inherits("global")

    log "warn"
    repositories {
        grailsPlugins()
        grailsCentral()
        mavenCentral()
    }
    dependencies {
        compile "org.apache.jclouds.provider:cloudfiles-us:1.6.2-incubating"
        runtime "org.jclouds:jclouds-compute:1.6.0"
    }
    plugins {
        runtime ":hibernate:$grailsVersion"
        build(":tomcat:$grailsVersion", ":release:2.0.3", ":rest-client-builder:1.0.2") {
            export = false
        }
    }

}