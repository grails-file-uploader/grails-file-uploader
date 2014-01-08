package com.lucastex.grails.fileuploader.cdn;

import java.io.File;

import org.jclouds.blobstore.BlobStore;
import org.jclouds.blobstore.BlobStoreContext;

public abstract class CDNFileUploader {

    public String accessKey;
    public String accessSecret;

    public BlobStore blobStore;
    public BlobStoreContext context;

    public abstract boolean authenticate();

    public abstract void close();

    public abstract boolean createContainer(String name);

    public abstract boolean uploadFile(String containerName, File file, String fileName);

}