/*
 * Copyright (c) 2016, CauseCode Technologies Pvt Ltd, India.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or
 * without modification, are not permitted.
 */

import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import grails.util.Environment

GString loggingPattern = "%d{HH:mm:ss.SSS} %-5level [${hostname}] %logger - %msg%n"

// For logging to console.
appender('STDOUT', ConsoleAppender) {
    encoder(PatternLayoutEncoder) {
        pattern = loggingPattern
    }
}

def logfileDate = timestamp("yyyyMMdd'T'HHmmss")
GString logFilePath = "${System.properties['user.home']}/logs/causecode/${logfileDate}.log"

if (Environment.current in [Environment.DEVELOPMENT, Environment.TEST]) {

    // Enable Spring Framework logs by passing the argument like 'grails -Dspring.logs=1 run-app'.
    if (System.properties['spring.logs'] == '1') {
        logger('org.springframework', DEBUG, ['STDOUT'], false)
    }
}

logger('grails.app', DEBUG, ['STDOUT'], false)
logger('com.causecode', DEBUG, ['STDOUT'], false)
logger('com.causecode', INFO, ['STDOUT'])