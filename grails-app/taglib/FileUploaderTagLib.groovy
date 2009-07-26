import org.codehaus.groovy.grails.web.taglib.exceptions.GrailsTagException

class FileUploaderTagLib {
	
	static namespace = 'fileuploader'
	
	static Long _byte  = 1
	static Long _kbyte = 1	*	1000
	static Long _mbyte = 1 	* 	1000	*	1024
	static Long _gbyte = 1	*	1000	*	1024	*	1024
	
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
	}
	
	def prettysize = { attrs ->
		
		if (!attrs.size) {
			def errorMsg = "'size' attribute not found in file-uploader preetysize tag."
			log.error (errorMsg)
			throw new GrailsTagException(errorMsg)
		}
		
		def size = attrs.size
		def sb = new StringBuilder()
		if (size >= _byte && size < _kbyte) {
			sb.append(size).append("b")
		} else if (size >= _kbyte && size < _mbyte) {
			size = size / _kbyte
			sb.append(size).append("kb")
		} else if (size >= _mbyte && size < _gbyte) {
			size = size / _mbyte
			sb.append(size).append("mb")
		} else if (size >= _gbyte) {
			size = size / _gbyte
			sb.append(size).append("gb")
		}
		out << sb.toString()
	}

}
/*
(0 - 1000) size = bytes
(1000 - 1000*1024) size / 1000 = kbytes
(1000*1024 - 1000*1024*1024) size / (1000 * 1024) = mbytes
(else) size / (1000 * 1024 * 1024) = gbytes
*/