package com.causecode.fileuploader

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
