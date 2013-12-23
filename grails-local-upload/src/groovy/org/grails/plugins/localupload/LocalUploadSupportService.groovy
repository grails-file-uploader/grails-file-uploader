package org.grails.plugins.localupload

import java.util.List
import java.util.Map;

import org.grails.plugins.localupload.UFile;

/**
 * Default version of localUploadListService.
 * If you want to use the ajax views, this service should be created in your 
 * app to return a list of UFile objects for a given request.
 */
class LocalUploadSupportService implements ILocalUploadSupportService {

	@Override
	/**
	 * Simply returns an empty map, no previously uploaded items for your domain
	 * model will be displayed in the ajax form
	 */
	List<UFile> listFor(Map params) {
		return []
	}

	@Override
	/**
	 * Default version that does not associate a ufile with your model.  It
	 * simply returns immediately
	 */
	void associateUFile(UFile ufile, Map params){
		return
	}
}
