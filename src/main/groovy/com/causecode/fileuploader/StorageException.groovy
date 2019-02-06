package com.causecode.fileuploader

/**
 * Exception class
 * Exception is thrown if any Storage related error is encountered.
 */
class StorageException extends Exception {

    StorageException(String msg) {
        super(msg)
    }

    StorageException(String message, Throwable throwable) {
        super(message, throwable)
    }
}
