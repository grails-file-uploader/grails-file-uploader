package com.lucastex.grails.fileuploader.cdn;

import java.io.File;

import org.jclouds.blobstore.BlobStore;
import org.jclouds.blobstore.BlobStoreContext;
import org.jclouds.cloudfiles.CloudFilesClient;

public abstract class CDNFileUploader {

    public String accessKey;

    public String accessSecret;

    public BlobStore blobStore;

    public CloudFilesClient cloudFilesClient;

    public BlobStoreContext context;

    public abstract boolean authenticate();

    public abstract void close();

    public abstract boolean containerExists(String name);

    public abstract boolean createContainer(String name);

    public abstract void deleteFile(String containerName, String fileName);

    public abstract boolean makeFilePublic(String containerName, String fileName);

    public abstract boolean uploadFile(String containerName, File file, String fileName);

}