package org.grails.plugins.localupload

import grails.converters.JSON

import org.grails.plugins.localupload.LocalUploadService;
import org.grails.plugins.localupload.UFile;
import org.springframework.context.MessageSource
import org.springframework.http.HttpStatus
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.multipart.MultipartHttpServletRequest

import org.grails.plugins.localupload.ILocalUploadSecurityService;
import org.grails.plugins.localupload.ILocalUploadSupportService;
import org.grails.plugins.localupload.LocalUploadServiceException;

class LocalUploadController {
	
	MessageSource messageSource
	
	LocalUploadService localUploadService
	
	ILocalUploadSecurityService localUploadSecurityService
	
	ILocalUploadSupportService localUploadSupportService

	private Map ufileToAjaxResult(UFile ufile){
		return [
			name: ufile.name,
			size: ufile.size,
			url: createLink(action:'download', id:ufile.id),
			thumbnail_url: ufile.pathToThumbnail,
			delete_url: createLink(action:'ajaxDeleteFile', id:ufile.id),
			delete_type: "DELETE"
		]
	}
	
	def ajaxUpload(){
		def results = []
		
		//upload group
		String upload = params.upload
		
		switch(request.method){
			/* If we get a get response, we'll just return back a list of ufiles
			 * that this request can access
			 */
			case "GET":
				List<UFile> ufiles = localUploadSupportService.listFor(params)
				
				if(ufiles){
					for(UFile ufile: ufiles){
						results << ufileToAjaxResult(ufile)
					}
				}
				
				break
			
			// accept files
			case "POST":
				if (request instanceof MultipartHttpServletRequest){
					for(filename in request.getFileNames()){
						MultipartFile file = request.getFile(filename)
						UFile ufile
						try{
							ufile = localUploadService.saveFile(upload, file, filename, request.locale)
							localUploadSupportService.associateUFile(ufile, params)
							results << ufileToAjaxResult(ufile)
						}catch(LocalUploadServiceException e){
							log.error("Failed to save File", e)
							render status: HttpStatus.INTERNAL_SERVER_ERROR.value()
							return
						}
					}
					
				}else{
					log.error("Received a post request that was not a MultipartHttpServletRequest")
					render status: HttpStatus.BAD_REQUEST.value()
					return
				}
				break
			
			// If we didn't receive get or post, error out
			default: 
				render status: HttpStatus.METHOD_NOT_ALLOWED.value()
		}

		render results as JSON
	}
	
	def upload(){

		//upload group
		String upload = params.upload
		
		String overrideFileName = params.overrideFileName
		
		//request file
		def file = request.getFile("file")
		
		UFile ufile
		
		try{
			ufile = localUploadService.saveFile(upload, file, overrideFileName, request.locale)
			localUploadSupportService.associateUFile(ufile, params)
		}catch(LocalUploadServiceException e){
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
			ufile = localUploadService.ufileById(params.id, request.locale)
			
			if (!localUploadSecurityService.allowed(ufile)){
				log.error = 'Not permitted access to $ufile'
				flash.message = message(code: "fileupload.security.notpermitted", args: [params.id])
				redirect controller: params.errorController, action: params.errorAction
				return
			}
		
			file = localUploadService.fileForUFile(ufile, request.locale)
			
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
	
	def ajaxDelete() {
		UFile ufile
		
		try{
			ufile = localUploadService.ufileById(params.id, request.locale)
			
			if (!localUploadSecurityService.allowedToDelete(ufile)){
				log.error("Not permitted to delete $ufile")
				render status: HttpStatus.UNAUTHORIZED.value()
				return
			}
		}catch(Exception e){
			log.error("Failed to find File", e)
			render status: HttpStatus.INTERNAL_SERVER_ERROR.value()
			return
		}
		
		if(!localUploadService.deleteFile(ufile)){
			log.error("Failed to delete $ufile")
			render status: HttpStatus.INTERNAL_SERVER_ERROR.value()
		}
		
		def result = [success: true]
		render result as JSON
	}
	
	def deleteFile() {
		UFile ufile
		
		try{
			ufile = localUploadService.ufileById(params.id, request.locale)
			
			if (!localUploadSecurityService.allowedToDelete(ufile)){
				log.error = 'Not permitted access to delete $ufile'
				flash.message = message(code: "fileupload.security.notpermitted", args: [params.id])
				redirect controller: params.errorController, action: params.errorAction
				return
			}
		}catch(Exception e){
			redirect controller: params.errorController, action: params.errorAction, params:(params.errorParams)
			return
		}
		
		if(localUploadService.deleteFile(ufile)){
			redirect controller: params.successController, action: params.successAction, params:(params.successParams)
		}else{
			redirect controller: params.errorController, action: params.errorAction, params:(params.errorParams)
		}
	}
}
