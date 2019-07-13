/*
 * Copyright (c) 2016, CauseCode Technologies Pvt Ltd, India.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or
 * without modification, are not permitted.
 */
package com.causecode.fileuploader

/**
 * Exception class
 * Exception is thrown if any error is encountered during file upload to CDN.
 */
class UploadFailureException extends StorageException {

    UploadFailureException(String message, Throwable throwable) {
        super(message, throwable)
    }

    UploadFailureException(String fileName, String containerName, Throwable throwable) {
        super("Could not upload file $fileName to container $containerName", throwable)
    }
}

