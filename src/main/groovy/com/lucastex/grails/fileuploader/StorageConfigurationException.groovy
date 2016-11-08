package com.lucastex.grails.fileuploader

class StorageConfigurationException extends Exception {

    StorageConfigurationException(String msg) {
        super(msg)
    }

    StorageConfigurationException(String msg, Throwable cause) {
        super(msg, cause)
    }
}
