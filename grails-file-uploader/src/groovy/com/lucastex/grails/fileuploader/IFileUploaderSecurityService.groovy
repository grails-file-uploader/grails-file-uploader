package com.lucastex.grails.fileuploader

/**
 * Interface that you can implement to secure access to individual files
 *
 */
interface IFileUploaderSecurityService {

	/**
	 * Controller passes the file that the user is trying to access.  You'll
	 * need to fetch the current user either from the session, or from your 
	 * security service
	 */
	boolean allowed(UFile ufile)
	
}
