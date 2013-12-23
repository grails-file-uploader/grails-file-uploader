package com.bowerstudios.fileManager

import com.lucastex.grails.fileuploader.IFileUploaderSecurityService
import com.lucastex.grails.fileuploader.UFile;

class FileUploaderSecurityService implements IFileUploaderSecurityService {

	@Override
	boolean allowed(UFile ufile) {
		log.info("Allowing access to $ufile")
		return true
	}

}
