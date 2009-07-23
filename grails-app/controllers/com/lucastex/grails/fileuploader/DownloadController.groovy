package com.lucastex.grails.fileuploader

class DownloadController {
	
	def messageSource
	
    def index = { 
	
		UFile ufile = UFile.get(params.id)
		if (!ufile) {
			log.debug "Invalid download request. There is no file with id=${params.id}"
			flash.message = messageSource.getMessage("fileupload.download.nofile", [params.id] as Object[], request.locale)
			redirect controller: params.errorController, action: params.errorAction
			return
		}
		
		//if no contentDisposition is supplied, will set attachment
		params.contentDisposition ?: "attachment"
		
		ufile.downloads++
		ufile.save()
		
		log.debug "Downloading file id=[${ufile.id}] for the ${ufile.downloads} time"
		
		def file = new File(ufile.path)
		response.setContentType("application/octet-stream")
		response.setHeader("Content-disposition", "${params.contentDisposition};filename=${file.name}")
		response.outputStream << file.text
		return
	}
}
