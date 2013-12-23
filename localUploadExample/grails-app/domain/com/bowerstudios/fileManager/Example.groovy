package com.bowerstudios.fileManager

import org.grails.plugins.localupload.UFile

class Example {

	String firstName
	String lastName
	
	static hasMany = [files: UFile]
}
