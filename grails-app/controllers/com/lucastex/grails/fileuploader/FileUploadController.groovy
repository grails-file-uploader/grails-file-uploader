class FileUploadController {
	
	//messagesource
	def messageSource

	//defaultaction
	def defaultAction = "process"

	def process = {

		//config handler
		def config = grailsApplication.config.fileupload
		
		//request file
		def file = request.getFile('file')
		
		//check if file exists
		if (!file) {
			log.debug "FileUpload pluging received no file to upload. Rejecting request."
			flash.message = messageSource.message('fileupload.nofile')
			redirect controller:params.errorController, action:params.errorAction
		}

	}

}
