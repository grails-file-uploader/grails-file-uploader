/*
 * Copyright (c) 2011-Present, CauseCode Technologies Pvt Ltd, India.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or
 * without modification, are not permitted.
 */
package com.causecode.fileuploader.logger

import org.junit.rules.ExternalResource
import org.slf4j.Logger

import java.lang.reflect.Field
import java.lang.reflect.Modifier

/**
 * This class is used to set the new field value for log in our test files, i.e. makes the log field
 * accessible using reflection and sets the value.
 *
 * Useful link - http://stackoverflow.com/questions/25022453/unit-testing-of-a-class-with-staticloggerbinder
 */
class ReplaceSlf4jLogger extends ExternalResource {
    Field logField
    Logger logger
    Logger originalLogger

    ReplaceSlf4jLogger(Class logClass, Logger logger) {
        logField = logClass.getDeclaredField('log')
        this.logger = logger
    }

    @Override
    protected void before() throws Throwable {
        logField.accessible = true

        Field modifiersField = Field.getDeclaredField('modifiers')
        modifiersField.accessible = true
        modifiersField.setInt(logField, logField.modifiers & ~Modifier.FINAL)

        originalLogger = (Logger) logField.get(null)
        logField.set(null, logger)
    }

    @Override
    protected void after() {
        logField.set(null, originalLogger)
    }

}
