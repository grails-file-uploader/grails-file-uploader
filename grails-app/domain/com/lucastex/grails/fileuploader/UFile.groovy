package com.lucastex.grails.fileuploader

import grails.util.Environment;

class UFile {

    transient fileUploaderService
    transient grailsApplication

    int downloads

    CDNProvider provider

    Date dateUploaded = new Date()
    Date expiresOn

    Long size

    String extension
    String fileGroup
    String name
    String path

    UFileType type

    static constraints = {
        expiresOn nullable: true, min: new Date()
        size min: 0L
        path blank: false
        name blank: false
        fileGroup blank: false
        provider nullable: true
    }

    def afterDelete() {
        fileUploaderService.deleteFileForUFile(this)
    }

    String searchLink() {
        fileUploaderService.resolvePath(this)
    }

    boolean canMoveToCDN() {
        type == UFileType.LOCAL
    }

    boolean isFileExists() {
        new File(path).exists()
    }

    String getContainer() {
        containerName(grailsApplication.config.fileuploader[fileGroup].container)
    }

    String getFullName() {
        name + "." + extension
    }

    @Override
    String toString() {
        "UFile [$id][$fileGroup][$type]"
    }

    static String containerName(String containerName) {
        if (Environment.current != Environment.PRODUCTION) {
            return containerName + "-" + Environment.current.name
        }
        return containerName
    }
}

enum UFileType {
    CDN_PRIVATE(1), CDN_PUBLIC(2), LOCAL(3)

    final int id
    UFileType(int id) {
        this.id = id
    }
}

enum CDNProvider {

    AMAZON(1),
    RACKSPACE(2)

    final int id
    CDNProvider(int id) {
        this.id = id
    }
}