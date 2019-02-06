package com.causecode.fileuploader

/**
 * Exception class.
 * This exception is thrown when Storage operations fail. Such as
 * When error is encountered while locating a file or while deleting any file from a container in Google CDN.
 * When error is encountered while creating a new container or deleting an existing one.
 * When authentication fails for Google CDN related operations.
 */
class GoogleStorageException extends StorageException {

    GoogleStorageException(String message) {
        super(message)
    }

    GoogleStorageException(String message, Throwable throwable) {
        super(message, throwable)
    }
}
