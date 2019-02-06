package com.causecode.fileuploader.util.checksum.exceptions

import com.causecode.fileuploader.UFile

/**
 * Exception which will be thrown when calculated checksum with given algorithm founds to be in database
 * @author Milan Savaliya
 * @since 3.1.0
 */
class DuplicateFileException extends IOException {
    UFile duplicateUFile

    DuplicateFileException(GString gString, UFile duplicateUFile) {
        super(gString.toString())

        this.duplicateUFile = duplicateUFile
    }
}
