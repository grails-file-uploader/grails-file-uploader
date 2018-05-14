/*
 * Copyright (c) 2011 - Present, CauseCode Technologies Pvt Ltd, India.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or
 * without modification, are not permitted.
 */
package com.causecode.fileuploader.provider

import com.causecode.fileuploader.ProviderNotFoundException
import com.causecode.fileuploader.cdn.CDNFileUploader
import grails.core.GrailsApplication

/**
 * This service class contains business logic related to different providers.
 *
 * @author Hardik Modha
 * @since 3.1.3
 */
class ProviderService {

    GrailsApplication grailsApplication

    /**
     * This method is used for dynamically instantiating the CDNFileUploader class based on the Provider.
     *
     * @param providerName The name of the provider.
     * @return Instance of the CDNFileUploader class.
     *
     * @author Nikhil Sharma
     * @since 2.4.9
     */
    CDNFileUploader getProviderInstance(String providerName) throws ProviderNotFoundException {
        String packageName = "com.causecode.fileuploader.cdn.${providerName.toLowerCase()}."
        String classNamePrefix = providerName.toLowerCase().capitalize()
        String providerClassName = packageName + "${classNamePrefix}CDNFileUploaderImpl"

        try {
            return grailsApplication.classLoader.loadClass(providerClassName)?.newInstance()
        } catch (ClassNotFoundException e) {
            log.debug 'Could not find Provider class', e
            throw new ProviderNotFoundException("Provider $providerName not found.", e)
        }
    }
}
