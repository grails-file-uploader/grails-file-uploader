import org.grails.plugins.localupload.FileSizeUtils

class LocalUploadTagLib {
	
	static namespace = 'localUpload'
	
	static final Set downloadBaseAttribs = ['errorAction', 'errorController', 'saveAssocId', 'fileId']
	
	/**
	 * Create a download link for a ufile
	 * @attr fileId The UFile id
	 * @attr saveAssocId The id of the domain object we're associating with, defaults to params.id
	 * @attr errorController The controller to redirect to in case of an error, defaults to params.controller
	 * @attr errorAction The action to redirect to in case of an error, defaults to params.action
	 * @attr contentDisposition 
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
		linkParams.errorAction = attrs.errorAction ?: actionName
		linkParams.errorController = attrs.errorController ?: controllerName
		
		// grab the id of the domain object we're associating with
		linkParams.saveAssocId = attrs.saveAssocId ?: (params?.id)
		
		linkParams.contentDisposition = attrs.contentDisposition ?: 'inline'
		
		// basic attributes to pass to the link builder
		def linkAttribs = [controller: "localUpload", action: "download", params: linkParams, id: attrs.fileId]
		
		// pass through extra attribs
		def remainingKeys = attrs.keySet() - downloadBaseAttribs
		remainingKeys.each{key ->
			linkAttribs.put(key, attrs[key])
		}
		
		out << g.link(linkAttribs, body)
	}
	
	static final Set minUploadBaseAttribs = ['bucket', 'name', 'multiple']
	/**
	 * Create just the fields necessary to add files. 
	 * Should be used within an existing form.
	 * Passes through any additional attribs to an input type="file" element
	 * @attr bucket LocalUpload group/bucket to which this file should be uploaded
	 * @attr name name of files field to submit to your form
	 * @attr multiple boolean true if should allow multiple files to be uploaded
	 */
	def minupload = { attrs, body ->
		
		//checking required fields
		if (!attrs.bucket) {
			def errorMsg = "'bucket' attribute not found in localUpload minupload tag."
			log.error (errorMsg)
			throwTagError(errorMsg)
		}
		
		//upload group
		def bucket = attrs.bucket
		def name = attrs.name ?: 'files'
		
		def tagBody = """<input type="hidden" name="bucket" value="${bucket}" />"""
		tagBody += """<input type="hidden" name="fileParam" value="${name}"/>"""
		tagBody += """<input type="file" name="${name}" """
		
		if(attrs.multiple){
			tagBody += 'multiple="multiple" '
		}
		
		// pass through extra attribs
		def remainingKeys = attrs.keySet() - minUploadBaseAttribs
		remainingKeys.each{key ->
			tagBody += """${key}="${attrs[key]}" """
		}
		
		tagBody += '/>'
		out << tagBody
	}
	
	/**
	 * Create a form to upload files to an existing domain object
	 * @attr bucket LocalUpload group/bucket to which this file should be uploaded
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
		def successAction = attrs.successAction ?: actionName
		def successController = attrs.successController ?: controllerName
		
		//case error
		def errorAction = attrs.errorAction ?: actionName
		def errorController = attrs.errorController ?: controllerName
		
		def tagBody = """<input type="hidden" name="bucket" value="${bucket}" />"""
		tagBody += """<input type="hidden" name="saveAssoc" value="${attrs.saveAssoc}" />"""
		tagBody += """<input type="hidden" name="errorAction" value="${errorAction}" />"""
		tagBody += """<input type="hidden" name="errorController" value="${errorController}" />"""
		tagBody += """<input type="hidden" name="successAction" value="${successAction}" />"""
		tagBody += """<input type="hidden" name="successController" value="${successController}" />"""
		tagBody += """<input type="file" name="files" """
		
		if(attrs.multiple){
			tagBody += 'multiple="multiple"/>'
		}else{
			tagBody += '/>'
		}
		
		//optional parameters for success action
		if(attrs.successParams) {
			if(attrs.successParams instanceof Map){
				attrs.successParams.each{k,v ->
					tagBody += """<input type="hidden" name="successParams.${k}" value="${v}" />"""
				}
			}else if(attrs.successParams instanceof List){
				attrs.successParams.eachWithIndex{v,k ->
					tagBody += """<input type="hidden" name="successParams.${k}" value="${v}" />"""
				}
			}else{
				tagBody += """<input type="hidden" name="successParams" value="${attrs.successParams}" />"""
			}
		}
		
		tagBody += """<input type="submit" name="submit" value="Submit" />"""
		
		//form build
		out << g.uploadForm([controller: 'localUpload', action: 'upload', id:attrs.id], tagBody)
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
		
		out << FileSizeUtils.prettySizeFromBytes(attrs['size'])
	}

}
