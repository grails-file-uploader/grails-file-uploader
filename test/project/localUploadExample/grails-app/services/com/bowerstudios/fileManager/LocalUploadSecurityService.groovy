package com.bowerstudios.fileManager

import org.grails.plugins.localupload.ILocalUploadSecurityService
import org.grails.plugins.localupload.UFile;

class LocalUploadSecurityService implements ILocalUploadSecurityService {

	@Override
	boolean allowed(UFile ufile) {
		log.info("Allowing access to $ufile")
		return true
	}
	
	@Override
	boolean allowedToDelete(UFile ufile) {
		return true
	}

}
