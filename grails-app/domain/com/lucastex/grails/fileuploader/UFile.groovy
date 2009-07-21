package com.lucastex.grails.fileuploader

class UFile {

	Long size
	String path
	String name
	String extension
	Date dateUploaded
	String description
	Integer downloads

    static constraints = {
		size(min:0L)
		path()
		name()
		extension()
		dateUploaded()
		description(nullable:true, blank:true)
		downloads()
    }
}
