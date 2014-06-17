package org.grails.plugins.localupload

import groovy.transform.CompileStatic

@CompileStatic
interface ILocalUploadSupportService {

	/**
	 * Pass a list of files back for the given search params
	 */
	List<UFile> listFor(Map params)
	
	/**
	 * After the LocalUpload call successfully persists the file and UFile 
	 * domain objects, it will call this method so that you may associate the
	 * UFile objects with your domain model.
	 */
	void associateUFiles(List<UFile> ufiles, Map params)
	
	/**
	 * Before the LocalUploader can remove a file, it will call this method so
	 * that you may remove the association between the UFile objects and your
	 * domain model
	 */
	void deassociateUFiles(List<UFile> ufiles)
}
