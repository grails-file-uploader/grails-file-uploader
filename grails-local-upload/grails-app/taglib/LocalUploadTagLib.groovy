import org.codehaus.groovy.grails.web.taglib.exceptions.GrailsTagException

class LocalUploadTagLib {
	
	static namespace = 'localUpload'
	
	static Long _byte  = 1
	static Long _kbyte = 1	*	1000
	static Long _mbyte = 1 	* 	1000	*	1024
	static Long _gbyte = 1	*	1000	*	1024	*	1024
	
	/**
	 * Create a download link for a ufile
	 * @attr fileId The UFile id
	 * @attr saveAssocId The id of the domain object we're associating with, defaults to params.id
	 * @attr errorController The controller to redirect to in case of an error, defaults to params.controller
	 * @attr errorAction The action to redirect to in case of an error, defaults to params.action
	 */
	def download = { attrs, body ->
		
		//checking required fields
		if (!attrs.fileId) {
			def errorMsg = "'fileId' attribute not found in localUpload:download tag."
			log.error (errorMsg)
			throwTagError(errorMsg)
		}
		
		def linkParams = [:]
		
		/* If a specific error action and controller has been specified, use them.
		 * Otherwise, just use the current action and controller
		 */
		linkParams.errorAction = attrs.errorAction ? attrs.errorAction : params.action
		linkParams.errorController = attrs.errorController ? attrs.errorController : params.controller
		
		//grab the id of the domain object we're associating with
		linkParams.saveAssocId = attrs.saveAssocId ? attrs.saveAssocId : params.id
		
		out << g.link([controller: "localUpload", action: "download", params: linkParams, id: attrs.fileId], body)
		
	}
	
	/**
	 * Create a form to upload files to an existing domain object
	 * @attr upload LocalUpload group/bucket to which this file should be uploaded
	 * @attr multiple boolean true if should allow multiple files to be uploaded
	 * @attr saveAssoc Key that is passed along to your implementation of ILocalUploadSupportService
	 * @attr successParams request params passed to success controller and action
	 * @attr successController The controller to redirect, defaults to params.controller
	 * @attr successAction The action to redirect, defaults to params.action
	 * @attr errorController The controller to redirect to in case of an error, defaults to params.controller
	 * @attr errorAction The action to redirect to in case of an error, defaults to params.action
	 */
	def form = { attrs ->
		
		//checking required fields
		if (!attrs.bucket) {
			def errorMsg = "'bucket' attribute not found in localUpload form tag."
			log.error (errorMsg)
			throwTagError(errorMsg)
		}
		
		if(!attrs.saveAssoc){
			def errorMsg = "'saveAssoc' attribute not found in localUpload form tag."
			log.error (errorMsg)
			throwTagError(errorMsg)
		}
		
		//upload group
		def bucket = attrs.bucket
		
		//case success
		def successAction = attrs.successAction ? attrs.successAction : params.action
		def successController = attrs.successController ? attrs.successController : params.controller
		
		//case error
		def errorAction = attrs.errorAction ? attrs.errorAction : params.action
		def errorController = attrs.errorController ? attrs.errorController : params.controller
		
		def tagBody = """
			<input type="hidden" name="bucket" value="${bucket}" />
			<input type="hidden" name="saveAssoc" value="${attrs.saveAssoc}" />
			<input type="hidden" name="errorAction" value="${errorAction}" />
			<input type="hidden" name="errorController" value="${errorController}" />
			<input type="hidden" name="successAction" value="${successAction}" />
			<input type="hidden" name="successController" value="${successController}" />
		"""
		
		if(attrs.multiple){
			tagBody += """<input type="file" name="files" multiple="multiple"/>"""
		}else{
			tagBody += """<input type="file" name="files" />"""
		}
		
		//optional parameters for success action
		if(attrs.successParams) {
			tagBody += """<input type="hidden" name="successParams" 
                        value="${attrs.successParams}" />"""
		}
		
		tagBody += """<input type="submit" name="submit" value="Submit" />"""
		
		//form build
		StringBuilder sb = new StringBuilder()
		sb.append g.uploadForm([controller: 'localUpload', action: 'upload', id:attrs.id], tagBody)
		
		out << sb.toString()
	}
	
	/**
	 * @attr size REQUIRED the value in bytes to convert
	 */
	def prettysize = { attrs ->
		if (!attrs['size']) {
			def errorMsg = "'size' attribute not found in local-upload prettysize tag."
			log.error (errorMsg)
			throwTagError(errorMsg)
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