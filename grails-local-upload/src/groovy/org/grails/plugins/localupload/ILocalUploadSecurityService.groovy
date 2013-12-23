package org.grails.plugins.localupload

import org.grails.plugins.localupload.UFile;

/**
 * Interface that you can implement to secure access to individual files
 *
 */
interface ILocalUploadSecurityService {

	/**
	 * Controller passes the file that the user is trying to access.  You'll
	 * need to fetch the current user either from the session, or from your 
	 * security service
	 */
	boolean allowed(UFile ufile)
	
	/**
	 * Controller passes the file that the user is trying to delete.  You'll 
	 * need to fetch the current user either from the session, or from your 
	 * security service
	 */
	boolean allowedToDelete(UFile ufile)
	
}
