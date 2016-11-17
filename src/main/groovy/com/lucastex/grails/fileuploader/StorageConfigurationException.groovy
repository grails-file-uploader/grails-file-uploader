/*
 * Copyright (c) 2016, CauseCode Technologies Pvt Ltd, India.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or
 * without modification, are not permitted.
 */
package com.lucastex.grails.fileuploader

/**
 * Exception class
 */
class StorageConfigurationException extends Exception {

    StorageConfigurationException(String msg) {
        super(msg)
    }

    StorageConfigurationException(String msg, Throwable cause) {
        super(msg, cause)
    }
}
