/*
 * Copyright (c) 2016, CauseCode Technologies Pvt Ltd, India.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or
 * without modification, are not permitted.
 */
package com.causecode.fileuploader.cdn.google

import com.google.cloud.storage.StorageException
import com.causecode.fileuploader.GoogleStorageException
import com.causecode.fileuploader.StorageConfigurationException
import com.causecode.fileuploader.UploadFailureException
import com.causecode.fileuploader.cdn.CDNFileUploader
import com.google.cloud.storage.Blob
import com.google.cloud.storage.BlobId
import com.google.cloud.storage.BlobInfo
import com.google.cloud.storage.Bucket
import com.google.cloud.storage.BucketInfo
import com.google.cloud.storage.Storage
import groovy.util.logging.Slf4j

import java.util.concurrent.TimeUnit
import javax.activation.MimetypesFileTypeMap

/**
 * This class is used for all the Google Cloud Storage operations.
 *
 * @author Nikhil Sharma
 * @since 2.4.9
 */
@Slf4j
class GoogleCDNFileUploaderImpl extends CDNFileUploader {

    static Storage gStorage

    GoogleCDNFileUploaderImpl() {
        authenticate()
    }

    Blob getBlob(String containerName, String fileName) {
        BlobId blobId = BlobId.of(containerName, fileName)
        try {
            return gStorage.get(blobId)
        } catch (StorageException e) {
            throw new GoogleStorageException("Could not find file ${fileName}", e)
        }
    }

    @Override
    boolean authenticate() throws GoogleStorageException {
        try {
            gStorage = gStorage ?: new GoogleCredentials().storage
        } catch (StorageConfigurationException e) {
            throw new GoogleStorageException('Could not authenticate GoogleCDNFileUploader', e)
        }

        return gStorage ? true : false
    }

    @Override
    void close() {
        // TODO Need to fix this. Not setting it to null so as to set the working auth step to default.
        // gStorage = null
    }

    @Override
    boolean containerExists(String name) {
        try {
            gStorage.get(name)
        } catch (StorageException e) {
            throw new GoogleStorageException("Could not find container $name", e)
        }

        return true
    }

    @Override
    boolean createContainer(String name) {
        Bucket bucket
        try {
            bucket = gStorage.create(BucketInfo.of(name))
        } catch (StorageException e) {
            throw new GoogleStorageException('Could not create container.', e)
        }

        log.debug "Container with name ${bucket.name()} successfully created."

        return true
    }

    @Override
    void deleteFile(String containerName, String fileName) {
        Blob blob = getBlob(containerName, fileName)

        if (!blob.delete(Blob.BlobSourceOption.generationMatch())) {
            throw new GoogleStorageException("Could not delete file $fileName from container $containerName")
        }

        log.debug "Successfully deleted file $fileName from container $containerName"
    }

    @Override
    String getPermanentURL(String containerName, String fileName) {
        Blob blob = getBlob(containerName, fileName)

        return blob?.mediaLink()
    }

    @Override
    String getTemporaryURL(String containerName, String fileName, long expiration) {
        Blob blob = getBlob(containerName, fileName)

        return blob?.signUrl(expiration, TimeUnit.SECONDS)?.toString()
    }

    @Override
    boolean makeFilePublic(String containerName, String fileName) {
        // TODO Add support for making the URL public by modifying the ACL. The service account needs permissions.
        return false
    }

    @Override
    boolean uploadFile(String containerName, File file, String fileName, boolean makePublic, long maxAge) {
        String contentType = new MimetypesFileTypeMap().getContentType(fileName)

        BlobId blobId = BlobId.of(containerName, fileName)
        BlobInfo blobInfo = BlobInfo.builder(blobId).contentType(contentType).build()

        try {
            gStorage.create(blobInfo, file.bytes)
        } catch (StorageException e) {
            throw new UploadFailureException(fileName, containerName, e)
        }

        log.debug "Successfully uploaded file $fileName"

        return true
    }
}
