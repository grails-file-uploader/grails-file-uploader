package com.lucastex.grails.fileuploader.cdn.amazon

import org.jclouds.ContextBuilder
import org.jclouds.blobstore.BlobStoreContext
import org.jclouds.blobstore.domain.Blob

import com.lucastex.grails.fileuploader.cdn.CDNFileUploader;

class AmazonCDNFileUploaderImpl extends CDNFileUploader {

    AmazonCDNFileUploaderImpl(String accessKey, String accessSecret) {
        this.accessKey = accessKey
        this.accessSecret = accessSecret
    }

    @Override
    boolean authenticate() {
        context = ContextBuilder.newBuilder("aws-s3")
                .credentials(accessKey, accessSecret)
                .buildView(BlobStoreContext.class)
        println "Context created ${context.class}"

        blobStore = context.getBlobStore()
        println "Blobstore ${blobStore.class}"

        return false
    }

    @Override
    void close() {
    }

    @Override
    boolean createContainer(String name) {
        blobStore.createContainerInLocation(null, name)
    }

    @Override
    boolean uploadFile(String containerName, File file, String fileName) {
        Blob newFileToUpload = blobStore.blobBuilder(fileName)
                .payload(file)
                .build()

        String eTag = blobStore.putBlob(containerName, newFileToUpload)
        return true
    }
}