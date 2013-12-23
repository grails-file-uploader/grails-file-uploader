package com.lucastex.grails.fileuploader

class FileUploaderSecurityService implements IFileUploaderSecurityService {

	@Override
	boolean allowed(UFile ufile) {
		return true
	}

}
