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
		
		out << g.link([controller: "fileUploader", action: "download", params: params, id: attrs.id], body)
		
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
                //optional parameters for success action
                if(attrs.successParams) {
                    tagBody += """<input type='hidden' name='successParams' 
                        value='${attrs.successParams}' />"""
                }
		
		//form build
		StringBuilder sb = new StringBuilder()
		sb.append g.uploadForm([controller: 'fileUploader', action: 'upload', id:attrs.id], tagBody)
		
		out << sb.toString()
	}
	
	/**
	 * @attr size REQUIRED the value to convert
	 */
	def prettysize = { attrs ->
		if (!attrs['size']) {
			def errorMsg = "'size' attribute not found in file-uploader prettysize tag."
			log.error (errorMsg)
			throw new GrailsTagException(errorMsg)
		}
		
		BigDecimal valSize = new BigDecimal(attrs['size'])	
		
		def sb = new StringBuilder()
		if (valSize >= _byte && valSize < _kbyte) {
			sb.append(valSize).append("b")
		} else if (valSize >= _kbyte && valSize < _mbyte) {
			valSize = valSize / _kbyte
			sb.append(valSize).append("kb")
		} else if (valSize >= _mbyte && valSize < _gbyte) {
			valSize = valSize / _mbyte
			sb.append(valSize).append("mb")
		} else if (valSize >= _gbyte) {
			valSize = valSize / _gbyte
			sb.append(valSize).append("gb")
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