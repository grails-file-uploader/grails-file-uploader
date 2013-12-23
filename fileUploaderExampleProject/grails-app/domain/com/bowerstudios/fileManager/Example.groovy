package com.bowerstudios.fileManager

import com.lucastex.grails.fileuploader.UFile

class Example {

	String firstName
	String lastName
	
	static hasMany = [files: UFile]
}
