package com.causecode.fileuploader

/**
 * Exception class
 * This exception is thrown when any operation that requires CDNProvider is unable to find a provider.
 */
class ProviderNotFoundException extends Exception {

    ProviderNotFoundException(String message) {
        super(message)
    }

    ProviderNotFoundException(String message, Throwable throwable) {
        super(message, throwable)
    }
}
