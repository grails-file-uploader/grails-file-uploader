package org.grails.plugins.localupload

import org.grails.plugins.localupload.UFile;

/**
 * No security version - You shouldn't use this.  Any user with permission to 
 * access the LocalUpload controller can get access to any file
 *
 */
class LocalUploadSecurityService implements ILocalUploadSecurityService {

	@Override
	boolean allowed(UFile ufile) {
		return true
	}
	
	@Override
	boolean allowedToDelete(UFile ufile) {
		return true
	}

}
