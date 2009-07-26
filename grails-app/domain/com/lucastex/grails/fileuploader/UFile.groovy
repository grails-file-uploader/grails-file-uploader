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
			if (f.delete()) {
				log.debug "file [${path}] deleted"
			} else {
				log.error "could not delete file: ${file}"
			}
		} catch (Exception exp) {
			log.error "Error deleting file: ${e.message}"
			log.error exp
		}
	}
}