package com.lucastex.grails.fileuploader


class UFile {

    Long size
    String path
    String name
    String extension
    Date dateUploaded
    Integer downloads
	
    static constraints = {
        size(min:0L)
        path()
        name()
        extension()
        dateUploaded()
        downloads()
    }

    def afterDelete() {
        try {
            File f = new File(path)
            if (f.exists()) {
                if (f.delete()) {
                    log.debug "File [${path}] deleted"
                    def timestampFolder = path.substring(0, path.lastIndexOf("/"))
                    new File(timestampFolder).delete()
                } else {
                    log.error "Could not delete file: ${f}"
                }
            }
        } catch (Exception exp) {
            log.error "Error deleting ufile: ${exp}"
        }
    }
}