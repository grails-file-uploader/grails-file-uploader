package org.grails.plugins.localupload

import org.grails.plugins.localupload.UFile;

interface ILocalUploadSupportService {

	/**
	 * Pass a list of files back for the given search params
	 */
	List<UFile> listFor(Map params)
	
	/**
	 * After the LocalUpload call successfully persists the file and UFile 
	 * domain object, it will call this method so that you may associate the
	 * UFile object with your domain model.
	 */
	void associateUFile(UFile ufile, Map params)
}
