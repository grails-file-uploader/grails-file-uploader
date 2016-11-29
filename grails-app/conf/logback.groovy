appender('STDOUT', ConsoleAppender) {
    encoder(PatternLayoutEncoder) {
        pattern = "%level %logger - %msg%n"
    }
}

logback = {
    error   'org.codehaus.groovy.grails.web.servlet'        // controllers
    'org.codehaus.groovy.grails.web.pages'           // GSP
    'org.codehaus.groovy.grails.web.mapping.filter' // URL mapping
    'org.codehaus.groovy.grails.web.mapping'        // URL mapping
    'org.codehaus.groovy.grails.commons'            // core / classloading
    'org.codehaus.groovy.grails.plugins'            // plugins
    'org.codehaus.groovy.grails.orm.hibernate'     // hibernate integration

    debug   'grails.app.filters.com.causecode', 'com.causecode',
            'grails.app.services.com.causecode.grails.fileuploader'

}

//root(DEBUG,['STDOUT'])
root(ERROR,['STDOUT'])