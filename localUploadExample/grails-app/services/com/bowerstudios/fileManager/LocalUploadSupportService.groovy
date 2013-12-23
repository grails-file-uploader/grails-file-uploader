package com.bowerstudios.fileManager

import java.util.Map;

import org.grails.plugins.localupload.ILocalUploadSupportService
import org.grails.plugins.localupload.UFile

class LocalUploadSupportService implements ILocalUploadSupportService {

	@Override
	List<UFile> listFor(Map params) {
		List<UFile> results = []
		
		if(params.saveAssoc){
			switch(params.saveAssoc){
				case 'example':
					results.addAll(Example.load(params.id)?.files)
					break
				default:
					break
			}
		}
		
		return results
	}

	@Override
	void associateUFile(UFile ufile, Map params) {
		println params
		
		Example example = Example.load(params.id)
		
		if(ufile){
			if(example.files){
				example.files.add(ufile)
			}else{
				example.files = [ufile]
			}
		}
		example.save(failOnError:true)

		return
	}

}
