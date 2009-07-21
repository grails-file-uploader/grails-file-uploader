package com.lucastex.grails.fileuploader

class UFile {
	
	String name
	String extension
	Long size
	String contentType
	byte[] stream
	Date dateUploaded

    static constraints = {
		name(maxSize:255)
		extension(blank:true)
		size(min:0)
		contentType()
		stream()
		dateUploaded()
    }
}
