/*
 * Copyright (c) 2016, CauseCode Technologies Pvt Ltd, India.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or
 * without modification, are not permitted.
 */
package com.causecode.grails.fileuploader

/**
 * Exception class
 * This exception is thrown when CDN configuration related error is encountered.
 */
class StorageConfigurationException extends Exception {

    StorageConfigurationException(String msg) {
        super(msg)
    }

    StorageConfigurationException(String msg, Throwable cause) {
        super(msg, cause)
    }
}
