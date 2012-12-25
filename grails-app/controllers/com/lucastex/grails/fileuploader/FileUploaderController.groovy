package com.lucastex.grails.fileuploader

import org.springframework.context.MessageSource

class FileUploaderController {
	
	MessageSource messageSource
	
	FileUploaderService fileUploaderService

	def upload(){

		//upload group
		String upload = params.upload
		
		String overrideFileName = params.overrideFileName
		
		//request file
		def file = request.getFile("file")
		
		UFile ufile
		
		try{
			ufile = fileUploaderService.saveFile(upload, file, overrideFileName, request.locale)
			
		}catch(FileUploaderServiceException e){
			flash.message = e.message
			redirect controller: params.errorController, action: params.errorAction, id: params.id
			return
			
		}
		
		redirect controller: params.successController, action: params.successAction, params:[ufileId:ufile.id, id: params.id,successParams:params.successParams]
	}
	
	def download() {
		
			UFile ufile
			File file
			
			try{
				ufile = fileUploaderService.ufileById(params.id, request.locale)
				file = fileUploaderService.fileForUFile(ufile, request.locale)
				
			}catch(FileNotFoundException fnfe){
				log.debug fnfe.message
				flash.message = fnfe.message
				redirect controller: params.errorController, action: params.errorAction
				return
				
			}catch(IOException ioe){
				log.error ioe.message
				flash.message = ioe.message
				redirect controller: params.errorController, action: params.errorAction
				return
			}
			
			log.debug "Serving file id=[${ufile.id}], downloaded for the ${ufile.downloads} time, to ${request.remoteAddr}"
			
			response.setContentType("application/octet-stream")
			response.setHeader("Content-disposition", "${params.contentDisposition}; filename=${ufile.name}")
			response.outputStream << file.readBytes()
	
			return
		}
	
	def deleteFile() {
		if(fileUploaderService.deleteFile(params.id)){
			redirect controller: params.successController, action: params.successAction, params:(params.successParams)
		}else{
			redirect controller: params.errorController, action: params.errorAction, params:(params.errorParams)
		}
	}
}
