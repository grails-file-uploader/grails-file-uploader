package com.causecode.fileuploader.util.checksum.exceptions

/**
 * Exception will be thrown when FileType for FileInputBean is not valid
 */
class UnRecognizedFileTypeException extends RuntimeException {

    UnRecognizedFileTypeException(String message) {
        super(message)
    }
}
