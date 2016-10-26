package com.lucastex.grails.fileuploader.cdn

class BlobDetail {

    Long ufileId
    String remoteBlobName
    File localFile
    String eTag

    BlobDetail(String remoteBlobName, File localFile, Long ufileId) {
        this(remoteBlobName, localFile, ufileId, "")
    }

    BlobDetail(String remoteBlobName, File localFile, Long ufileId, String eTag) {
        this.remoteBlobName = remoteBlobName
        this.localFile = localFile
        this.eTag = eTag
        this.ufileId = ufileId
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

    @Override
    public String toString() {
        "{$remoteBlobName}{$localFile}{$ufileId}"
    }

}