package com.lucastex.grails.fileuploader

import static com.google.common.base.Preconditions.checkArgument
import static java.util.concurrent.Executors.newFixedThreadPool

import java.util.concurrent.Callable

import org.jclouds.ContextBuilder
import org.jclouds.blobstore.BlobStore
import org.jclouds.blobstore.BlobStoreContext
import org.jclouds.blobstore.domain.Blob
import org.jclouds.cloudfiles.CloudFilesApiMetadata
import org.jclouds.cloudfiles.CloudFilesClient
import org.jclouds.openstack.swift.CommonSwiftAsyncClient
import org.jclouds.openstack.swift.CommonSwiftClient
import org.jclouds.openstack.swift.domain.ContainerMetadata
import org.jclouds.openstack.swift.domain.SwiftObject
import org.jclouds.rest.RestContext

import com.google.common.collect.Lists
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.ListeningExecutorService
import com.google.common.util.concurrent.MoreExecutors
import com.lucastex.grails.fileuploader.cdn.BlobDetail

class CDNFileUploaderService {

    static transactional = false

    def grailsApplication

    private static int THREADS = Integer.getInteger("upload.threadpool.size", 10)

    private BlobStore blobStore
    private CloudFilesClient cloudFilesClient
    private Set<ContainerMetadata> containers = []
    private RestContext<CommonSwiftClient, CommonSwiftAsyncClient> swift

    String uploadFileToCDN(String containerName, def file, String fileName) {
        SwiftObject object = swift.getApi().newSwiftObject()
        object.getInfo().setName(fileName)
        object.setPayload(file)
        swift.getApi().putObject(containerName, object)
        cdnEnableContainer(containerName)
    }

    void uploadFilesToCloud(String containerName, List<BlobDetail> blobDetails) {
        ListeningExecutorService executor = MoreExecutors.listeningDecorator(newFixedThreadPool(THREADS))
        List<ListenableFuture<BlobDetail>> blobUploaderFutures = Lists.newArrayList()

        blobDetails.each {
            BlobUploader blobUploader = new BlobUploader(containerName, it)
            ListenableFuture<BlobDetail> blobDetailFuture = executor.submit(blobUploader)
            blobUploaderFutures.add(blobDetailFuture)
        }

        ListenableFuture<List<BlobDetail>> future = Futures.successfulAsList(blobUploaderFutures)
        List<BlobDetail> uploadedBlobDetails = future.get() // begin the upload

        uploadedBlobDetails.each {
            BlobDetail blobInstance = blobDetails.find { blobDetailInstance ->
                blobDetailInstance.ufileId == it.ufileId
            }
            if(it) {
                blobInstance.eTag = it.eTag
                log.info "UFile [$blobInstance.ufileId] eTag: [$it.eTag] uploaded successfully."
            } else {
                log.info "UFile [$blobInstance.ufileId] eTag: [$it.eTag] uploaded failed."
            }
        }
    }

    boolean checkIfContainerExist(String containerName) {
        for(container in containers) {
            if(container.name == name) {
                return true
                break
            }
            return false
        }
    }

    boolean crateContainer(String containerName) {
        boolean success = swift.getApi().createContainer(containerName)
        listContainers()    // update container list
        success
    }

    void listContainers() {
        swift.getApi().listContainers()
        log.info "Containers" + containers
    }

    String cdnEnableContainer(String containerName) {
        URI cdnURI = cloudFilesClient.enableCDN(containerName)
        cdnURI.toString()
    }

    void authenticate() {
        String key = grailsApplication.config.fileuploader.CDNKey
        String username = grailsApplication.config.fileuploader.CDNUsername
        if(!key || !username) {
            log.info "No username or key configured for file uploader CDN service."
            return
        }

        BlobStoreContext context = ContextBuilder.newBuilder("cloudfiles-us")
                .credentials(username, key)
                .buildView(BlobStoreContext.class)
        blobStore = context.getBlobStore()
        swift = context.unwrap()
        cloudFilesClient = context.unwrap(CloudFilesApiMetadata.CONTEXT_TOKEN).getApi()
    }

    void deleteFile(String containerName, String fileName) {
        swift.api.removeObject(containerName, fileName)
    }

    void close() {
        blobStore?.getContext()?.close()
    }

    private class BlobUploader implements Callable<BlobDetail> {

        private final String container
        private final BlobDetail toBeUploadedBlobDetail

        protected BlobUploader(String container, BlobDetail toBeUploadedBlobDetail) {
            this.container = container
            this.toBeUploadedBlobDetail = toBeUploadedBlobDetail
        }

        @Override
        public BlobDetail call() throws Exception {
            Blob blob = blobStore.blobBuilder(toBeUploadedBlobDetail.getRemoteBlobName())
                    .payload(toBeUploadedBlobDetail.getLocalFile())
                    .contentType("") // allows Cloud Files to determine the content type
                    .build()
            String eTag = blobStore.putBlob(container, blob)
            BlobDetail uploadedBlobDetail = new BlobDetail(
                    toBeUploadedBlobDetail.getRemoteBlobName(), toBeUploadedBlobDetail.getLocalFile(),
                    toBeUploadedBlobDetail.ufileId, eTag)

            return uploadedBlobDetail
        }

    }

}