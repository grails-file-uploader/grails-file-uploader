/*
 * Copyright (c) 2016, CauseCode Technologies Pvt Ltd, India.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or
 * without modification, are not permitted.
 */
package com.causecode.fileuploader.cdn.google

import com.causecode.fileuploader.StorageConfigurationException
import com.google.auth.oauth2.OAuth2Utils
import com.google.auth.oauth2.ServiceAccountCredentials
import com.google.cloud.AuthCredentials
import com.google.cloud.storage.Storage
import com.google.cloud.storage.StorageOptions
import grails.util.Holders
import groovy.util.logging.Slf4j

/**
 * This file is used as a wrapper for the Google Credentials configuration.
 *
 * Authenticates the GCS in 3 ways,
 *
 * 1. Using direct credential values from Config object (File is not required in this case)
 * 2. Using config object to create AuthCredentials (Reading JSON key file's path from config object)
 * 3. Using default authentication (Reading JSON key file's path from environment variable)
 *
 * @author Nikhil Sharma
 * @since 2.5.2
 */
@Slf4j
@SuppressWarnings(['PropertyName', 'CyclomaticComplexity', 'CatchNullPointerException'])
class GoogleCredentials {

    String type
    String project_id
    String private_key_id
    String private_key
    String client_email
    String client_id

    ConfigObject googleCredentials

    /**
     * Initializes the configurations for Google Cloud Storage Authentication from the grails config object.
     *
     * @throws StorageConfigurationException
     *
     * @author Nikhil Sharma
     * @since 2.5.2
     */
    void initializeGoogleCredentialsFromConfig() throws StorageConfigurationException {
        googleCredentials = Holders.grailsApplication.config.fileuploader?.storageProvider?.google

        if (!googleCredentials) {
            throw new StorageConfigurationException('No configuration found for storage provider Google.')
        }

        this.project_id = googleCredentials.project_id
        this.type = googleCredentials.type ?: 'service_account'

        if (!this.project_id) {
            throw new StorageConfigurationException('Project Id is required for storage provider Google.')
        }
    }

    /**
     * Authenticates using the GOOGLE_APPLICATION_CREDENTIALS environment variable.
     *
     * @throws IllegalArgumentException
     * @return Object Instance of Storage
     *
     * @author Nikhil Sharma
     * @since 2.5.2
     */
    Storage authenticateUsingEnvironmentVariable() throws IllegalArgumentException {
        StorageOptions storageOptions = StorageOptions.defaultInstance()

        return storageOptions.service()
    }

    /**
     * Returns the Storage instance by authenticating with the provided AuthCredentials instance.
     *
     * @param authCredentials Object Instance of AuthCredentials
     * @throws IllegalArgumentException
     * @return Object Instance of Storage
     *
     * @author Nikhil Sharma
     * @since 2.5.2
     */
    Storage setAuthCredentialsAndAuthenticate(AuthCredentials authCredentials) throws IllegalArgumentException {
        StorageOptions.Builder builder = StorageOptions.builder()
        builder.authCredentials(authCredentials)
        builder.projectId(this.project_id)

        return builder.build().service()
    }

    /**
     * Authenticates the GCS by reading the JSON key file from the path provided in the Configuration object.
     *
     * @throws IOException
     * @throws IllegalArgumentException
     * @return Object Instance of Storage
     *
     * @author Nikhil Sharma
     * @since 2.5.2
     */
    Storage authenticateUsingKeyFileFromConfig() throws IOException, IllegalArgumentException {
        String keyFilePath = googleCredentials?.authFile

        if (!keyFilePath) {
            throw new IllegalArgumentException('JSON Key file path for storage provider Google not found.')
        }

        AuthCredentials authCredentials = AuthCredentials.createForJson(new FileInputStream(keyFilePath))

        return setAuthCredentialsAndAuthenticate(authCredentials)
    }

    /**
     * Creates a Map from fields of this class required for AuthCredentials.
     *
     * @return Map A Map that is parsed for retrieving credentials.
     *
     * @author Nikhil Sharma
     * @since 2.5.2
     */
    Map getCredentialsMap() {
        this.private_key_id = googleCredentials.private_key_id
        this.private_key = googleCredentials.private_key
        this.client_email = googleCredentials.client_email
        this.client_id = googleCredentials.client_id

        return [
            client_id: this.client_id,
            client_email: this.client_email,
            private_key: this.private_key,
            private_key_id: this.private_key_id
        ]
    }

    /**
     * Authenticates the GCS by directly reading the values defined in the Configuration object.
     *
     * @throws IOException
     * @throws NullPointerException
     * @throws IllegalArgumentException
     * @return Object Instance of Storage
     *
     * @author Nikhil Sharma
     * @since 2.5.2
     */
    Storage authenticateUsingValuesFromConfig() throws IOException, NullPointerException, IllegalArgumentException {
        ServiceAccountCredentials serviceAccountCredentials = ServiceAccountCredentials.fromJson(credentialsMap,
                OAuth2Utils.HTTP_TRANSPORT)

        AuthCredentials authCredentials = new AuthCredentials.ServiceAccountAuthCredentials(serviceAccountCredentials)

        return setAuthCredentialsAndAuthenticate(authCredentials)
    }

    /**
     * Authenticates the GCS in 3 ways,
     *
     * 1. Using direct credential values from Config object (File is not required in this case)
     * 2. Using config object to create AuthCredentials (Reading JSON key file's path from config object)
     * 3. Using default authentication (Reading JSON key file's path from environment variable)
     *
     * @throws StorageConfigurationException
     * @return Object Instance of Storage
     *
     * @author Nikhil Sharma
     * @since 2.5.2
     */
    Storage getStorage() throws StorageConfigurationException {
        try {
            initializeGoogleCredentialsFromConfig()
            return authenticateUsingValuesFromConfig()
        } catch (StorageConfigurationException | IllegalArgumentException | IOException | NullPointerException e) {
            log.debug 'Authentication using direct config values failed.', e

            try {
                return authenticateUsingKeyFileFromConfig()
            } catch (IllegalArgumentException | IOException e1) {
                log.debug 'Authentication by reading file from path defined in config failed.', e1

                try {
                    return authenticateUsingEnvironmentVariable()
                } catch (IllegalArgumentException e2) {
                    log.debug 'Authentication using GOOGLE_APPLICATION_CREDENTIALS environment variable failed for ' +
                            'Google Cloud Storage.', e2

                    throw new StorageConfigurationException('GCS Authentication failed due to bad configuration', e2)
                }
            }
        }
    }
}
