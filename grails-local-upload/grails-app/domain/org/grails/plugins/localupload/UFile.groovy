package org.grails.plugins.localupload

class UFile {

    Long size
    String path
    String name
    String extension
    Date dateUploaded
    Integer downloads
	String pathToThumbnail
	
    static constraints = {
        size(min:0L)
        path()
        name()
        extension()
        dateUploaded()
        downloads()
		pathToThumbnail nullable:true
    }

	String toString(){
		return path
	}
}