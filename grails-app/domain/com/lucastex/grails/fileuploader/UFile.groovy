package com.lucastex.grails.fileuploader

class UFile {

    transient fileUploaderService

    Long size
    String path
    String name
    String extension
    Date dateUploaded = new Date()
    int downloads

    UFileType type

    static constraints = {
        size min: 0L
        path blank: false
        name blank: false
    }

    def afterDelete() {
        fileUploaderService.deleteFileForUFile(new File(path))
    }

}

enum UFileType {
    CDN_PRIVATE, CDN_PUBLIC, LOCAL
}