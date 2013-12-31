package com.bowerstudios.fileManager

import java.util.Map;

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
	void associateUFile(UFile ufile, Map params) {
		
		if(ufile){
			switch(params.saveAssoc){
				case 'example':
					Example example = Example.load(params.id)
				
					if(example.files){
						example.files.add(ufile)
					}else{
						example.files = [ufile]
					}
				
					example.save(failOnError:true)
		
					break
				default:
					log.error("Save Association ${params.saveAssoc} not handled in associateUFile")
					break
			}
		}
		
		return
	}

}
