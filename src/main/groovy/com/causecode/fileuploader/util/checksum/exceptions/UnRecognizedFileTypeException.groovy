/*
 * Copyright (c) 2011 - Present, CauseCode Technologies Pvt Ltd, India.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or
 * without modification, are not permitted.
 */
package com.causecode.fileuploader.util.checksum.exceptions

/**
 * Exception will be thrown when FileType for FileInputBean is not valid
 * @author Milan Savaliya
 * @since 3.1.0
 */
class UnRecognizedFileTypeException extends RuntimeException {

    UnRecognizedFileTypeException(String message) {
        super(message)
    }
}
