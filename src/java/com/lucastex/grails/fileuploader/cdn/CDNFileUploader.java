package com.lucastex.grails.fileuploader.cdn;

import java.io.File;

import org.jclouds.blobstore.BlobStore;
import org.jclouds.blobstore.BlobStoreContext;
import org.jclouds.cloudfiles.CloudFilesClient;

/**
 * Abstract class to provide an interface so that any cloud provider can be
 * easily configured in combination with Apache jCloud.
 * 
 * Container term used may points to different meaning to different providers.
 * For example: bucket is used in Amazon S3 Service
 */
public abstract class CDNFileUploader {

    public String accessKey;

    public String accessSecret;

    public BlobStore blobStore;

    public CloudFilesClient cloudFilesClient;

    public BlobStoreContext context;

    /**
     * Used to authenticate with the Cloud provider API using username/key, password/secret pairs.
     * @return
     */
    public abstract boolean authenticate();

    /**
     * Used to close the context created by the `authenticate()` method.
     */
    public abstract void close();

    /**
     * Checks to see if any container exists or not.
     * @param name: Name of the container
     * @return
     */
    public abstract boolean containerExists(String name);

    /**
     * @param name: Name of the container
     * @return true if container created and false if container already exists
     */
    public abstract boolean createContainer(String name);

    /**
     * Deleting a object ( In terms of Amazone S3) or blob ( In terms of Apache JCloud)
     * @param containerName: Name of the container
     * @param fileName: Name of the file to delete
     */
    public abstract void deleteFile(String containerName, String fileName);

    /**
     * Used to get the permanent URL of the file stored on the cloud.
     * @param containerName
     * @param fileName
     * @return
     */
    public abstract String getPermanentURL(String containerName, String fileName);

    /**
     * Used to get a temporary URL for particular object or file.
     * For example: This is know as Pre-signed URL in amazon S3 service.
     * @param containerName
     * @param fileName
     * @param expiration
     * @return Temporary URL for specified file.
     */
    public abstract String getTemporaryURL(String containerName, String fileName, long expiration);

    /**
     * Used to make a file public, so that any user create browse the file through the link.
     * @param containerName
     * @param fileName
     * @return
     */
    public abstract boolean makeFilePublic(String containerName, String fileName);

    /**
     * Used to upload file to a cloud provider.
     * @param containerName
     * @param file
     * @param fileName: A fileName to used as key
     * @return
     */
    public abstract boolean uploadFile(String containerName, File file, String fileName, boolean makePublic);

}