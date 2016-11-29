/*
 * Copyright (c) 2016, CauseCode Technologies Pvt Ltd, India.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or
 * without modification, are not permitted.
 */
package com.causecode.grails.fileuploader.cdn

import com.causecode.grails.fileuploader.UFile

/**
 * Class to hold Blob related details
 */
class BlobDetail {

    UFile ufile
    String remoteBlobName
    File localFile
    String eTag

    BlobDetail(String remoteBlobName, File localFile, UFile uFile) {
        this(remoteBlobName, localFile, uFile, '')
    }

    BlobDetail(String remoteBlobName, File localFile, UFile ufile, String eTag) {
        this.remoteBlobName = remoteBlobName
        this.localFile = localFile
        this.eTag = eTag
        this.ufile = ufile
    }

    String getRemoteBlobName() {
        return remoteBlobName
    }

    File getLocalFile() {
        return localFile
    }

    String getETag() {
        return eTag
    }

    boolean isUploaded() {
        return eTag != null
    }

    @Override
    String toString() {
        "{$remoteBlobName}{$localFile}{$ufile.id}"
    }
}
