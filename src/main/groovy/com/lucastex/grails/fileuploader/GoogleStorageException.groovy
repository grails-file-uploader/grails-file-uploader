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
class GoogleStorageException extends StorageException {

    GoogleStorageException(String message) {
        super(message)
    }

    GoogleStorageException(String message, Throwable throwable) {
        super(message, throwable)
    }
}
