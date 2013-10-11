package com.lucastex.grails.fileuploader

class UFile {

    transient fileUploaderService
    transient grailsApplication

    int downloads

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

}

enum UFileType {
    CDN_PRIVATE, CDN_PUBLIC, LOCAL
}