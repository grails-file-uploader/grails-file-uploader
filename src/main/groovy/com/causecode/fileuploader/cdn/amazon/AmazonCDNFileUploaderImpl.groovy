package com.causecode.fileuploader.cdn.amazon

import com.causecode.fileuploader.UploadFailureException
import com.causecode.fileuploader.cdn.CDNFileUploader
import grails.util.Holders
import groovy.util.logging.Slf4j
import org.jclouds.ContextBuilder
import org.jclouds.aws.s3.AWSS3Client
import org.jclouds.aws.s3.blobstore.options.AWSS3PutObjectOptions
import org.jclouds.blobstore.BlobStoreContext
import org.jclouds.blobstore.KeyNotFoundException
import org.jclouds.http.HttpRequest
import org.jclouds.http.HttpResponseException
import org.jclouds.s3.domain.AccessControlList
import org.jclouds.s3.domain.AccessControlList.Permission
import org.jclouds.s3.domain.CannedAccessPolicy
import org.jclouds.s3.domain.S3Object
import org.jclouds.s3.domain.internal.MutableObjectMetadataImpl
import org.jclouds.s3.domain.internal.S3ObjectImpl
import org.jclouds.s3.options.CopyObjectOptions
import javax.activation.MimetypesFileTypeMap

/**
 * This class is used for all the Google Cloud Storage operations.
 */
@Slf4j
/**
 * TODO Investigate why codenarc throws error for not implementing Closeable even though Closeable has been implemented
 * in parent class (CDNFileUploader).
 */
class AmazonCDNFileUploaderImpl extends CDNFileUploader implements Closeable {

    AWSS3Client client

    AmazonCDNFileUploaderImpl() {
        String key = Holders.flatConfig['fileuploader.storageProvider.amazon.AmazonKey']
        String secret = Holders.flatConfig['fileuploader.storageProvider.amazon.AmazonSecret']

        if (!key || !secret) {
            log.warn 'No username or key configured for Amazon CDN service'
            return
        }

        this.accessKey = key
        this.accessSecret = secret

        authenticate()
    }

    @Override
    boolean authenticate() {
        context = ContextBuilder.newBuilder('aws-s3')
                .credentials(accessKey, accessSecret)
                .buildView(BlobStoreContext)
        log.info "Context created ${context.class}"

        blobStore = context.blobStore
        log.info "Blobstore ${blobStore.class}"

        // Storing wrapped api of S3Client with apache jcloud
        client = context.unwrap().api

        return true
    }

    @Override
    void close() {
        context?.close()
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
        getObject(containerName, fileName).metadata.uri
    }

    /**
     * @param containerName Name of the bucket
     * @param fileName Name of the object in bucket
     * @param expiration expiration time in seconds for pre-signed URl.
     *        For example: 60 * 60 // For 1 hour.
     *
     * @see 'http://docs.aws.amazon.com/AmazonS3/latest/dev/ShareObjectPreSignedURLJavaSDK.html'
     */
    @Override
    String getTemporaryURL(String containerName, String fileName, long expiration) {
        HttpRequest request = context.signer.signGetBlob(containerName, fileName, expiration)
        request.endpoint.toString()
    }

    @Override
    boolean makeFilePublic(String containerName, String fileName) {
        AccessControlList acl = new AccessControlList()
        acl.addPermission(new URI('http://acs.amazonaws.com/groups/global/AllUsers'), Permission.READ)
        getObject(containerName, fileName).setAccessControlList(acl)
    }

    @Override
    boolean uploadFile(String containerName, File file, String fileName, boolean makePublic, long maxAge) {

        CannedAccessPolicy cannedAccessPolicy = makePublic ? CannedAccessPolicy.PUBLIC_READ : CannedAccessPolicy.PRIVATE

        AWSS3PutObjectOptions fileOptions = new AWSS3PutObjectOptions()
        fileOptions.withAcl(cannedAccessPolicy)

        MutableObjectMetadataImpl mutableObjectMetadata = new MutableObjectMetadataImpl()
        mutableObjectMetadata.setKey(fileName)

        // Getting the content type of file from the file name
        String contentType = new MimetypesFileTypeMap().getContentType(fileName)

        S3Object s3ObjectToUpdate = new S3ObjectImpl(mutableObjectMetadata)

        s3ObjectToUpdate.setPayload(file)

        /**
         * It replaces the earlier created meta with the File metadata where it only sets the length of file in the
         * header.
         *
         * Line 131: {@link S3ObjectImpl}
         * public void setPayload(Payload data) {
         *      super.setPayload(data);
         *      metadata.setContentMetadata(data.getContentMetadata());
         * }
         *
         * After setting file payload, it replaces all the metadata with null values i.e metadata of file.
         * {@link  org.jclouds.io.payloads.FilePayload}
         */
        s3ObjectToUpdate.payload.contentMetadata.setContentDisposition("attachment; filename= ${fileName}")
        s3ObjectToUpdate.payload.contentMetadata.setContentType(contentType)

        log.info("Setting cache control in $fileName with max age $maxAge")

        s3ObjectToUpdate.payload.contentMetadata
                .setCacheControl("max-age=$maxAge, public, must-revalidate, proxy-revalidate")

        try {
            /* Throws HttpResponseException if the conditions requested set are not satisfied by the object
             * on the server.
             */
            client.putObject(containerName, s3ObjectToUpdate, fileOptions)
        } catch (HttpResponseException e) {
            throw new UploadFailureException(fileName, containerName, e)
        }

        return true
    }

    /**
     * This method is used to update meta data of previously uploaded file. Amazon doesn't allow to update the metadata
     * of already existing file. Hence, updating the metadata by copying the file with new metadata with cache control.
     *
     * @param containerName String the name of the bucket
     * @param fileName String the name of the file to update metadata
     * @param makePublic Boolean whether to make file public
     * @param maxAge long cache header's max age in seconds
     * @since 2.4.3
     * @author Priyanshu Chauhan
     */
    void updatePreviousFileMetaData(String containerName, String fileName, Boolean makePublic, long maxAge) {
        Map metaData = [:]
        String cacheControl = "max-age=$maxAge, public, must-revalidate, proxy-revalidate"
        metaData['Cache-Control'] = cacheControl

        metaData['Content-Type'] = new MimetypesFileTypeMap().getContentType(fileName)

        CopyObjectOptions copyObjectOptions = new CopyObjectOptions()
        copyObjectOptions.overrideMetadataWith(metaData)

        CannedAccessPolicy cannedAccessPolicy = makePublic ? CannedAccessPolicy.PUBLIC_READ : CannedAccessPolicy.PRIVATE
        copyObjectOptions.overrideAcl(cannedAccessPolicy)

        try {

            /* Copying the same file with the same name to the location so that we can override the previous file with
            * new meta data.
            */
            client.copyObject(containerName, fileName, containerName, fileName, copyObjectOptions)
        } catch (KeyNotFoundException e) {
            log.info("Blob cannot be located in the container for file $fileName")
        }
    }
}
