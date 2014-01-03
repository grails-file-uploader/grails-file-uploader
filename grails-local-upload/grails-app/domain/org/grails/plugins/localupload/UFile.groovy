package org.grails.plugins.localupload

class UFile {

    Long sizeInBytes
    String path
    String name
    String extension
    Date dateUploaded
    Integer downloads
	String pathToThumbnail
	
    static constraints = {
        sizeInBytes(min:0L)
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