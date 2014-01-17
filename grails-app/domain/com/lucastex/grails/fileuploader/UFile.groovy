package com.lucastex.grails.fileuploader

class UFile {

    transient fileUploaderService
    transient grailsApplication

    int downloads

    CDNProvider provider

    Date dateUploaded = new Date()

    Long size

    String extension
    String fileGroup
    String name
    String path

    UFileType type

    static constraints = {
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
        grailsApplication.config.fileuploader[fileGroup].container
    }

    String getFullName() {
        name + "." + extension
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