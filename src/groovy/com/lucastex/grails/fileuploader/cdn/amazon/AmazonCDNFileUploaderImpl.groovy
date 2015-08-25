package com.lucastex.grails.fileuploader.cdn.amazon

import grails.util.Holders

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.jclouds.ContextBuilder
import org.jclouds.aws.s3.AWSS3Client
import org.jclouds.aws.s3.blobstore.options.AWSS3PutObjectOptions
import org.jclouds.blobstore.BlobStoreContext
import org.jclouds.blobstore.domain.Blob
import org.jclouds.http.HttpRequest
import org.jclouds.s3.domain.AccessControlList
import org.jclouds.s3.domain.CannedAccessPolicy
import org.jclouds.s3.domain.S3Object
import org.jclouds.s3.domain.AccessControlList.Permission
import org.jclouds.s3.domain.internal.MutableObjectMetadataImpl
import org.jclouds.s3.domain.internal.S3ObjectImpl
import org.jclouds.openstack.swift.v1.*

import com.lucastex.grails.fileuploader.cdn.CDNFileUploader

class AmazonCDNFileUploaderImpl extends CDNFileUploader {

    private static Log log = LogFactory.getLog(this)

    AWSS3Client client

    AmazonCDNFileUploaderImpl(String accessKey, String accessSecret) {
        this.accessKey = accessKey
        this.accessSecret = accessSecret
    }

    static AmazonCDNFileUploaderImpl getInstance() {
        String key = Holders.getFlatConfig()["fileuploader.AmazonKey"]
        String secret = Holders.getFlatConfig()["fileuploader.AmazonSecret"]

        if (!key || !secret) {
            log.warn "No username or key configured for Amazon CDN service"
        }

        return new AmazonCDNFileUploaderImpl(key, secret)
    }

    @Override
    boolean authenticate() {
        context = ContextBuilder.newBuilder("aws-s3")
                .credentials(accessKey, accessSecret)
                .buildView(BlobStoreContext.class)
        println "Context created ${context.class}"

        blobStore = context.getBlobStore()
        println "Blobstore ${blobStore.class}"

        // Storing wrapped api of S3Client with apache jcloud
        client = context.unwrap().getApi()

        return false
    }

    @Override
    void close() {
        context.close()
    }

    @Override
    boolean containerExists(String name) {
        client.bucketExists(name)
    }

    @Override
    boolean createContainer(String name) {
        blobStore.createContainerInLocation(null, name)
    }

    @Override
    void deleteFile(String containerName, String fileName) {
        blobStore.removeBlob(containerName, fileName)
    }

    S3ObjectImpl getObject(String containerName, String fileName) {
        client.getObject(containerName, fileName, null)
    }

    @Override
    String getPermanentURL(String containerName, String fileName) {
//        getObject(containerName, fileName).metadata.uri
//        HttpRequest request = context.signer.signGetBlob(containerName, fileName, 100L)
        HttpRequest request = context.signer.signGetBlob(containerName, fileName)
        request.endpoint.toString()
    }

    /**
     * @param containerName Name of the bucket
     * @param fileName Name of the object in bucket
     * @param expiration expiration time in seconds for pre-signed URl.
     *        For example: 60 * 60 // For 1 hour.
     *
     * @see http://docs.aws.amazon.com/AmazonS3/latest/dev/ShareObjectPreSignedURLJavaSDK.html
     */
    @Override
    String getTemporaryURL(String containerName, String fileName, long expiration) {
        // GMT: Sun, 13 Sep 2020 12:26:40 GMT expiration date
        HttpRequest request = context.signer.signGetBlob(containerName, fileName, 1600000000)
        request.endpoint.toString()
    }

    @Override
    boolean makeFilePublic(String containerName, String fileName) {
        AccessControlList acl = new AccessControlList()
        acl.addPermission(new URI("http://acs.amazonaws.com/groups/global/AllUsers"), Permission.READ)
        getObject(containerName, fileName).setAccessControlList(acl)
    }

    @Override
    boolean uploadFile(String containerName, File file, String fileName, boolean makePublic, long maxAge) {
        if (makePublic) {
            updateS3ObjectMetaData(containerName, fileName, maxAge, file)
        } else {
            Blob newFileToUpload = blobStore.blobBuilder(fileName)
                    .payload(file)
                    .build()
            blobStore.putBlob(containerName, newFileToUpload)
        }

        return true
    }

    void updateS3ObjectMetaData(String containerName, String fileName, long maxAge, File newFile = null) {
        AWSS3PutObjectOptions fileOptions = new AWSS3PutObjectOptions()
        fileOptions.withAcl(CannedAccessPolicy.AUTHENTICATED_READ)

        MutableObjectMetadataImpl mutableObjectMetadata

        if (newFile) {
            mutableObjectMetadata = new MutableObjectMetadataImpl()
            mutableObjectMetadata.setKey(fileName)
        } else {
            S3ObjectImpl s3Object = getObject(containerName, fileName)
            mutableObjectMetadata = s3Object?.getMetadata()
        }

        if (!mutableObjectMetadata) {
            log.info("No meta data found for $fileName")
            println "No meta data found for $fileName"
            return
        }
        
        mutableObjectMetadata.setCacheControl("max-age=$maxAge, public, must-revalidate, proxy-revalidate")
        log.info("Setting cache control in $fileName with max age $maxAge")
        println("Setting cache control in $fileName with max age $maxAge")

        S3Object s3ObjectToUpdate = new S3ObjectImpl(mutableObjectMetadata)

        if (newFile) {
            s3ObjectToUpdate.setPayload(newFile)
        } else {
            s3ObjectToUpdate.setPayload(fileName)
            return
        }
        client.putObject(containerName, s3ObjectToUpdate, fileOptions)
    }
}