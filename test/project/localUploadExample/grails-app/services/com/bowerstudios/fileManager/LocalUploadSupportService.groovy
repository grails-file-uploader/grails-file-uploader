package com.bowerstudios.fileManager

import org.grails.plugins.localupload.ILocalUploadSupportService
import org.grails.plugins.localupload.UFile

class LocalUploadSupportService implements ILocalUploadSupportService {

	@Override
	List<UFile> listFor(Map params) {
		List<UFile> results = []
		
		switch(params.saveAssoc){
			case 'example':
				results.addAll(Example.load(params.id)?.files)
				break
			default:
				log.error("Save Association ${params.saveAssoc} not handled in listFor")
				break
		}
		
		return results
	}

	@Override
	void associateUFiles(List<UFile> ufiles, Map params) {
	
		if(!ufiles){
			return
		}

		Example example

		switch(params.saveAssoc){
			case 'example':
				example = Example.get(params.id)
				break
			default:
				log.warn("Save Association ${params.saveAssoc} not handled in associateUFile")
				return
		}
			
		for(UFile ufile : ufiles){
			example.addToFiles(ufile)
		}

		example.save(flush:true)
	}
	
	@Override
	void deassociateUFiles(List<UFile> ufiles){
		
		if(!ufiles){
			return
		}
		
		for(UFile ufile in ufiles){
			def examples = Example.withCriteria {
				files {
					eq('id', ufile.id)
				}
			}

			for(Example example in examples){
				example.removeFromFiles(ufile)
				example.save(flush:true)
			}

		}

	}
}
