import org.codehaus.groovy.grails.web.taglib.exceptions.GrailsTagException

class FileUploaderTagLib {
	
	static namespace = 'fileuploader'
	
	def download = { attrs, body ->
		
		//checking required fields
		if (!attrs.id) {
			def errorMsg = "'id' attribute not found in file-uploader download tag."
			log.error (errorMsg)
			throw new GrailsTagException(errorMsg)
		}
				
		if (!attrs.errorAction) {
			def errorMsg = "'errorAction' attribute not found in file-uploader form tag."
			log.error (errorMsg)
			throw new GrailsTagException(errorMsg)
		}
		
		if (!attrs.errorController) {
			def errorMsg = "'errorController' attribute not found in file-uploader form tag."
			log.error (errorMsg)
			throw new GrailsTagException(errorMsg)
		}
		
		params.errorAction = attrs.errorAction
		params.errorController = attrs.errorController
		
		out << g.link([controller: "download", action: "index", params: params, id: attrs.id], body)
		
	}
	
	def form = { attrs ->
		
		//checking required fields
		if (!attrs.upload) {
			def errorMsg = "'upload' attribute not found in file-uploader form tag."
			log.error (errorMsg)
			throw new GrailsTagException(errorMsg)
		}
		
		if (!attrs.successAction) {
			def errorMsg = "'successAction' attribute not found in file-uploader form tag."
			log.error (errorMsg)
			throw new GrailsTagException(errorMsg)
		}
		
		if (!attrs.successController) {
			def errorMsg = "'successController' attribute not found in file-uploader form tag."
			log.error (errorMsg)
			throw new GrailsTagException(errorMsg)
		}
		
		if (!attrs.errorAction) {
			def errorMsg = "'errorAction' attribute not found in file-uploader form tag."
			log.error (errorMsg)
			throw new GrailsTagException(errorMsg)
		}
		
		if (!attrs.errorController) {
			def errorMsg = "'errorController' attribute not found in file-uploader form tag."
			log.error (errorMsg)
			throw new GrailsTagException(errorMsg)
		}
		
		//upload group
		def upload = attrs.upload
		
		//case success
		def successAction = attrs.successAction
		def successController = attrs.successController
		
		//case error
		def errorAction = attrs.errorAction
		def errorController = attrs.errorController
		
		def tagBody = """
			<input type='hidden' name='upload' value='${upload}' />
			<input type='hidden' name='errorAction' value='${errorAction}' />
			<input type='hidden' name='errorController' value='${errorController}' />
			<input type='hidden' name='successAction' value='${successAction}' />
			<input type='hidden' name='successController' value='${successController}' />
			<input type='file' name='file' />
			<input type='submit' name='submit' value='Submit' />
		"""
		
		//form build
		StringBuilder sb = new StringBuilder()
		sb.append g.uploadForm([controller: 'fileUploader', action: 'process'], tagBody)
		
		out << sb.toString()
		
		//if (!attrs.controller)
		//	throw new GrailsTagException("'controller' attribute not found in file-uploader form tag")
		//	
		//if (!attrs.action)
		//	throw new GrailsTagException("'action' attribute not found in file-uploader form tag")
		
		
	}

}
