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
			log.debug "FileUploader pluging received no file to upload. Rejecting request."
			flash.message = messageSource.getMessage("fileupload.nofile", null, request.locale)
			redirect controller: params.errorController, action: params.errorAction
			return
		}
		
		/***********************
			check extensions
		************************/
		println "FILE NAME = ${file.originalFilename}"
		def fileExtension = file.originalFilename.substring(file.originalFilename.lastIndexOf('.')+1)
		println "FILE EXTENSION: ${fileExtension}"
		println "ALLOWED FILE EXTENSIONS: ${config.allowedExtensions}"
		if (!config.allowedExtensions[0].equals("*")) {
			if (!config.allowedExtensions.contains(fileExtension)) {
				log.debug "FileUploader plugin received a file with an unauthorized extension (${fileExtension}). Permitted extensions ${config.allowedExtensions}"
				flash.message = messageSource.getMessage("fileupload.unauthorizedExtension", [fileExtension] as Object[], request.locale)
				redirect controller: params.errorController, action: params.errorAction
				return
			}
		}
		
		
		/*********************
			check file size
		**********************/
		if (config.maxSize) { //if maxSize config exists
			def maxSizeInKb = ((int) (config.maxSize/1024))
			if (file.size > config.maxSize) { //if filesize is bigger than allowed
				log.debug "FileUploader plugin received a file bigger than allowed. Max file size is ${maxSizeInKb}"
				flash.message = messageSource.getMessage("fileupload.fileBiggerThanAllowed", [maxSizeInKb] as Object[], request.locale)
				redirect controller: params.errorController, action: params.errorAction
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
		
		log.debug "FileUploader plugin received a ${file.size}b file. Moving to ${path}"
		file.transferTo(new File(path))
		
		def ufile = new UFile()
		ufile.name = file.name
		ufile.size = file.size
		ufile.extension = fileExtension
		ufile.dateUploaded = new Date(currentTime)
		ufile.path = path
		ufile.downloads = 0
		ufile.save()
		
		redirect controller: params.successController, action: params.successAction
	}

}
