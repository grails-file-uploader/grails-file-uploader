/*
 * Copyright (c) 2011 - Present, CauseCode Technologies Pvt Ltd, India.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or
 * without modification, are not permitted.
 */
package com.causecode.fileuploader.util.checksum.exceptions

/**
 * Exception which will be thrown when calculated checksum with given algorithm founds to be in database
 * @author Milan Savaliya
 * @since 3.1.0
 */
class DuplicateFileException extends RuntimeException {
    DuplicateFileException(GString gString) {
        super(gString.toString())
    }
}
