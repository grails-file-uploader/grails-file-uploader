/*
 * Copyright (c) 2011 - Present, CauseCode Technologies Pvt Ltd, India.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or
 * without modification, are not permitted.
 */
package com.causecode.fileuploader.util.checksum.exceptions

import com.causecode.fileuploader.UFile

/**
 * Exception which will be thrown when calculated checksum with given algorithm founds to be in database
 * @author Milan Savaliya
 * @since 3.1.0
 */
class DuplicateFileException extends RuntimeException {
    UFile duplicateUFile

    DuplicateFileException(GString gString) {
        super(gString.toString())
    }

    DuplicateFileException(GString gString, UFile duplicateUFile) {
        super(gString.toString())

        this.duplicateUFile = duplicateUFile
    }
}
