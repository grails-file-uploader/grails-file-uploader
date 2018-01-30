package com.causecode.fileuploader.util.checksum.exceptions

/**
 * Exception which will be thrown when calculated checksum with given algorithm founds to be in database
 * @author Milan Savaliya
 */
class CalculatedChecksumRefersToExistingFileException extends RuntimeException {
    def CalculatedChecksumRefersToExistingFileException(GString gString) {
        super(gString.toString())
    }
}
