package com.bowerstudios.fileManager

import java.util.Map;

import org.grails.plugins.localupload.ILocalUploadSupportService
import org.grails.plugins.localupload.UFile

class LocalUploadSupportService implements ILocalUploadSupportService {

	@Override
	List<UFile> listFor(Map params) {
		println params
		
		
		return []
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
