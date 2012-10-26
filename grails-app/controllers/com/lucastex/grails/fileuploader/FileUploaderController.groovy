package com.lucastex.grails.fileuploader

class FileUploaderController {
	
	//messagesource
	def messageSource

	//defaultaction
	def defaultAction = "process"

	def process = {

		//upload group
		def upload = params.upload
				
		//config handler
		def config = grailsApplication.config.fileuploader[upload]
		
		//request file
		def file = request.getFile("file")
		
		//base path to save file
		def path = config.path
		if (!path.endsWith('/'))
			path = path+"/"
		
		/**************************
			check if file exists
		**************************/
		if (file.size == 0) {
			def msg = messageSource.getMessage("fileupload.upload.nofile", null, request.locale)
			log.debug msg
			flash.message = msg
			redirect controller: params.errorController, action: params.errorAction, id: params.id
			return
		}
		
		/***********************
			check extensions
		************************/
		def fileExtension = file.originalFilename.substring(file.originalFilename.lastIndexOf('.')+1)
		if (!config.allowedExtensions[0].equals("*")) {
			if (!config.allowedExtensions.contains(fileExtension)) {
				def msg = messageSource.getMessage("fileupload.upload.unauthorizedExtension", [fileExtension, config.allowedExtensions] as Object[], request.locale)
				log.debug msg
				flash.message = msg
				redirect controller: params.errorController, action: params.errorAction, id: params.id
				return
			}
		}
		
		
		/*********************
			check file size
		**********************/
		if (config.maxSize) { //if maxSize config exists
			def maxSizeInKb = ((int) (config.maxSize/1024))
			if (file.size > config.maxSize) { //if filesize is bigger than allowed
				log.debug "FileUploader plugin received a file bigger than allowed. Max file size is ${maxSizeInKb} kb"
				flash.message = messageSource.getMessage("fileupload.upload.fileBiggerThanAllowed", [maxSizeInKb] as Object[], request.locale)
				redirect controller: params.errorController, action: params.errorAction, id: params.id
				return
			}
		} 
		
		//reaches here if file.size is smaller or equal config.maxSize or if config.maxSize ain't configured (in this case
		//plugin will accept any size of files).
		
		//sets new path
		def currentTime = System.currentTimeMillis()
		path = path+currentTime+"/"
		if (!new File(path).mkdirs())
			log.error "FileUploader plugin couldn't create directories: [${path}]"
		path = path+file.originalFilename
		
		//move file
		log.debug "FileUploader plugin received a ${file.size}b file. Moving to ${path}"
		file.transferTo(new File(path))
		
		//save it on the database
		def ufile = new UFile()
		ufile.name = file.originalFilename
		ufile.size = file.size
		ufile.extension = fileExtension
		ufile.dateUploaded = new Date(currentTime)
		ufile.path = path
		ufile.downloads = 0
		ufile.save()
		
		redirect controller: params.successController, action: params.successAction, params:[ufileId:ufile.id, id: params.id,successParams:params.successParams]
	}
    
    def show={
			def ufile = UFile.get(params.int("id"))
	        if (!ufile) {
	          response.sendError(404)
	          return;
	        }
			def file = new File(ufile.path)
			if (file.exists()) {
				response.setContentType("image/"+ufile.extension)
		        response.setContentLength(file.size().toInteger())
		        OutputStream out = response.getOutputStream();
				out.write(file.bytes)
		        out.close();
			}
	
	}

}
