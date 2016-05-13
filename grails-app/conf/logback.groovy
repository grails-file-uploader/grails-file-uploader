appender('STDOUT', ConsoleAppender) {
    encoder(PatternLayoutEncoder) {
        pattern = "%level %logger - %msg%n"
    }
}

logback = {
    error    'org.codehaus.groovy.grails.web.servlet',        // controllers
             'org.codehaus.groovy.grails.web.pages',          // GSP
             'org.codehaus.groovy.grails.web.mapping.filter', // URL mapping
             'org.codehaus.groovy.grails.web.mapping',        // URL mapping
             'org.codehaus.groovy.grails.commons',            // core / classloading
             'org.codehaus.groovy.grails.plugins',            // plugins
             'org.codehaus.groovy.grails.orm.hibernate',      // hibernate integration
             'org.springframework',
             'org.hibernate'

    debug    'grails.app.conf', 'grails.app.controllers', 'grails.app.services.com.causecode',
             'grails.app.taglib.com.causecode', 'grails.app.domain.com.lucastex.grails.fileuploader',
             'grails.app.jobs', 'grails.app.filters.com.causecode', 'com.causecode',
             'grails.app.services.com.lucastex.grails.fileuploader'

    info    'grails.app.conf'
            'grails.app.filters'
            'grails.app.taglib'
            'grails.app.services'
            'grails.app.controllers'
            'grails.app.domain'
            'org.codehaus.groovy.grails.commons'
            'org.codehaus.groovy.grails.web'
            'org.codehaus.groovy.grails.web.mapping'
            'org.codehaus.groovy.grails.plugins'
            'grails.spring'
            'org.springframework'
            'org.hibernate'
}

root(DEBUG,['STDOUT'])