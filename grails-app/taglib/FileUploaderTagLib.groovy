import org.codehaus.groovy.grails.web.taglib.exceptions.GrailsTagException

class FileUploaderTagLib {
	
	static namespace = 'fileuploader'
	
	def form = { attrs ->
		
		if (!attrs.upload) {
			def errorMsg = "'upload' attribute not found in file-uploader form tag."
			log.error (errorMsg)
			throw new GrailsTagException(errorMsg)
		}
		
		def successAction = attrs.successAction
		def successController = attrs.successController
		
		def errorAction = attrs.errorAction
		def errorController = attrs.errorController
		
		StringBuilder sb = new StringBuilder()
		sb.append g.uploadForm(controller: 'fileUpload', action: 'process') {
			g.hiddenField name: 'errorAction', value: errorAction
			g.hiddenField name: 'errorController', value: errorController
			g.hiddenField name: 'successAction', value: successAction
			g.hiddenField name: 'errorController', value: errorController
			
		}
		
		out << sb.toString()
		
		//if (!attrs.controller)
		//	throw new GrailsTagException("'controller' attribute not found in file-uploader form tag")
		//	
		//if (!attrs.action)
		//	throw new GrailsTagException("'action' attribute not found in file-uploader form tag")
		
		
	}

}
