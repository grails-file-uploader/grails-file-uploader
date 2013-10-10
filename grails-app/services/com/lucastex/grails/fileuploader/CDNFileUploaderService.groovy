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
        println swift.api.class
        object.getInfo().setName(fileName)
        object.setPayload(file)
        println swift.getApi().putObject(containerName, object)
        cdnEnableContainer(containerName)
    }

    void saveFileToCDN(String containerName) {
        ListeningExecutorService executor = MoreExecutors.listeningDecorator(newFixedThreadPool(THREADS))
        List<ListenableFuture<BlobDetail>> blobUploaderFutures = Lists.newArrayList()

        List<BlobDetail> blobDetails = [new BlobDetail("test-dir/test-img.gif", new File("./web-app/images/star.gif"))]

        for (BlobDetail blobDetail: blobDetails) {
            BlobUploader blobUploader = new BlobUploader(containerName, blobDetail)
            ListenableFuture<BlobDetail> blobDetailFuture = executor.submit(blobUploader)
            blobUploaderFutures.add(blobDetailFuture)
        }

        ListenableFuture<List<BlobDetail>> future = Futures.successfulAsList(blobUploaderFutures)
        List<BlobDetail> uploadedBlobDetails = future.get() // begin the upload

        uploadedBlobDetails.each {
            if(it) {
                System.out.format("  %s (eTag: %s)%n", it.getRemoteBlobName(), it.getETag())
            } else {
                System.out.format(" %s (ERROR)%n", it.getLocalFile().getAbsolutePath())
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
        println "Containers" + containers
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

    void close() {
        blobStore?.getContext()?.close()
    }

    public static class BlobDetail {
        private final String remoteBlobName
        private final File localFile
        private final String eTag

        protected BlobDetail(String remoteBlobName, File localFile) {
            this(remoteBlobName, localFile, null)
        }

        protected BlobDetail(String remoteBlobName, File localFile, String eTag) {
            this.remoteBlobName = remoteBlobName
            this.localFile = localFile
            this.eTag = eTag
        }

        public String getRemoteBlobName() {
            return remoteBlobName
        }

        public File getLocalFile() {
            return localFile
        }

        public String getETag() {
            return eTag
        }

        public boolean isUploaded() {
            return eTag != null
        }
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
                    toBeUploadedBlobDetail.getRemoteBlobName(), toBeUploadedBlobDetail.getLocalFile(), eTag)

            return uploadedBlobDetail
        }
    }

}