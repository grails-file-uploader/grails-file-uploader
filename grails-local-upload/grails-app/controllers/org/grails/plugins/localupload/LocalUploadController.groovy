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

	/**
	 * Used to turn uFile results into ajax expected by bootstrap-file-upload 
	 * @param ufile
	 * @return map properties expected by bootstrap-file-upload 
	 */
	private Map ufileToAjaxResult(UFile ufile){
		return [
			name: ufile.name,
			size: ufile.sizeInBytes,
			url: createLink(action:'download', id:ufile.id),
			thumbnail_url: ufile.pathToThumbnail,
			delete_url: createLink(action:'ajaxDeleteFile', id:ufile.id),
			delete_type: "DELETE"
		]
	}
	
	/**
	 * Used to feed ajax to the bootstrap-file-upload plugin
	 */
	def ajaxUpload(){
		def results = []
		
		String bucket = params.bucket
		String fileParam = params.fileParam ?: 'files'
		
		switch(request.method){
			/* If we get a GET response, we'll just return back a list of ufiles
			 * that this request can access
			 */
			case "GET":
				List<UFile> ufiles = localUploadSupportService.listFor(params)
				
				if(ufiles){
					for(UFile ufile: ufiles){
						results.add(ufileToAjaxResult(ufile))
					}
				}
				
				break
			
			// accept files
			case "POST":
				if (request instanceof MultipartHttpServletRequest){
					for(MultipartFile file in request.getFiles(fileParam)){
						if(file.empty){
							continue
						}
						
						UFile ufile
						try{
							ufile = localUploadService.saveFile(bucket, file, file.originalFilename, request.locale)
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

	/**
	 * If you've already persisted your domain object, you can use this method 
	 * via the taglib (or raw) to upload a file and attach it to your domain object 
	 * 
	 * @param bucket  name of local upload bucket to place the files
	 * @param fileParam  name of request parameter containing file submissions
	 * @param errorController  where to go if upload fails
	 * @param errorAction  where to go if upload fails
	 * @param successController  where to go if upload succeeds
	 * @param successAction  where t go if upload succeeds
	 * @param id  id of domain object for success or error actions
	 */
	def upload(){

		String bucket = params.bucket
		String fileParam = params.fileParam ?: 'files'

		if (request instanceof MultipartHttpServletRequest){
			MultipartHttpServletRequest req = request
			for(MultipartFile file in req.getFiles(fileParam)){
				if(file.empty){
					continue
				}
				
				UFile ufile
				try{
					ufile = localUploadService.saveFile(bucket, file, file.originalFilename, request.locale)
					localUploadSupportService.associateUFile(ufile, params)
				}catch(LocalUploadServiceException e){
					flash.message = e.message
					redirect controller: params.errorController, action: params.errorAction, id: params.id
					return
				}
			}
			
			redirect controller: params.successController, action: params.successAction, params:[id: params.id,successParams:params.successParams]
			
		}else{
			log.error("Received a post request that was not a MultipartHttpServletRequest")
			flash.message = message(code: "localupload.upload.multipartExpected")
			redirect controller: params.errorController, action: params.errorAction, id: params.id
			return
		}
	}
	
	/**
	 * You should only use this method to access files, access to files is then
	 * secured with your implementation of ILocalUploadSecurityService
	 * 
	 * @param id  id of ufile to download
	 * @param errorController  where to go if upload fails
	 * @param errorAction  where to go if upload fails
	 * @param saveAssocId  id of domain object for error actions
	 * 
	 * @return
	 */
	def download() {
		
		UFile ufile
		File file
			
		try{
			ufile = localUploadService.ufileById(params.id, request.locale)
			
			if (!localUploadSecurityService.allowed(ufile)){
				log.error "Not permitted access to $ufile"
				flash.message = message(code: "localupload.security.notpermitted", args: [params.id])
				redirect controller: params.errorController, action: params.errorAction, id: params.saveAssocId
				return
			}
		
			file = localUploadService.fileForUFile(ufile, request.locale)
			
		}catch(FileNotFoundException fnfe){
			log.debug fnfe.message
			flash.message = fnfe.message
			redirect controller: params.errorController, action: params.errorAction, id: params.saveAssocId
			return
			
		}catch(IOException ioe){
			log.error ioe.message
			flash.message = ioe.message
			redirect controller: params.errorController, action: params.errorAction, id: params.saveAssocId
			return
		}
		
		log.info "Serving file id=[${ufile.id}], downloaded for the ${ufile.downloads} time, to ${request.remoteAddr}"
		
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
				log.error "Not permitted access to delete $ufile"
				flash.message = message(code: "localupload.security.notpermitted", args: [params.id])
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
