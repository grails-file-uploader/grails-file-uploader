/*
 * Copyright (c) 2016, CauseCode Technologies Pvt Ltd, India.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or
 * without modification, are not permitted.
 */
package com.causecode.fileuploader.cdn

import org.jclouds.blobstore.BlobStore
import org.jclouds.blobstore.BlobStoreContext
import org.jclouds.cloudfiles.CloudFilesClient

/**
 * Abstract class to provide an interface so that any cloud provider can be
 * easily configured in combination with Apache jCloud.
 *
 * Container term used may points to different meaning to different providers.
 * For example: bucket is used in Amazon S3 Service
 */
abstract class CDNFileUploader implements Closeable {

    String accessKey
    String accessSecret
    BlobStore blobStore
    CloudFilesClient cloudFilesClient
    BlobStoreContext context

    /**
     * Used to authenticate with the Cloud provider API using username/key, password/secret pairs.
     * @return
     */
    abstract boolean authenticate()

    /**
     * Used to close the context created by the `authenticate()` method.
     */
    abstract void close()

    /**
     * Checks to see if any container exists or not.
     * @param name: Name of the container
     * @return
     */
    abstract boolean containerExists(String name)

    /**
     * @param name: Name of the container
     * @return true if container created and false if container already exists
     */
    abstract boolean createContainer(String name)

    /**
     * Deleting a object ( In terms of Amazone S3) or blob ( In terms of Apache JCloud)
     * @param containerName: Name of the container
     * @param fileName: Name of the file to delete
     */
    abstract void deleteFile(String containerName, String fileName)

    /**
     * Used to get the permanent URL of the file stored on the cloud.
     * @param containerName
     * @param fileName
     * @return
     */
    abstract String getPermanentURL(String containerName, String fileName)

    /**
     * Used to get a temporary URL for particular object or file.
     * For example: This is know as Pre-signed URL in amazon S3 service.
     * @param containerName
     * @param fileName
     * @param expiration
     * @return Temporary URL for specified file.
     */
    abstract String getTemporaryURL(String containerName, String fileName, long expiration)

    /**
     * Used to make a file public, so that any user create browse the file through the link.
     * @param containerName
     * @param fileName
     * @return
     */
    abstract boolean makeFilePublic(String containerName, String fileName)

    /**
     * Used to upload file to a cloud provider.
     * @param containerName
     * @param file
     * @param fileName: A fileName to used as key
     * @return
     */
    abstract boolean uploadFile(String containerName, File file, String fileName, boolean makePublic, long maxAge)
}
