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
        compile ("org.apache.jclouds.provider:cloudfiles-us:1.7.0", "org.apache.jclouds:jclouds-compute:1.7.0") {
            excludes "jclouds-core"
        }
        compile "org.apache.jclouds:jclouds-core:1.7.0"
    }
    plugins {
        runtime (":hibernate:$grailsVersion") {
            export = false
        }
        build(":tomcat:$grailsVersion", ":release:2.0.3", ":rest-client-builder:1.0.2") {
            export = false
        }
    }

}