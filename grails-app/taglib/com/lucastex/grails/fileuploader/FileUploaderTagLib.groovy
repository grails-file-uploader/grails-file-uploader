/*
 * Copyright (c) 2016, CauseCode Technologies Pvt Ltd, India.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or
 * without modification, are not permitted.
 */
package com.lucastex.grails.fileuploader

/**
 * A tag library that fulfills role of 'view helper' in the Model View Controller (MVC) pattern and
 * helps with GSP rendering
 */
@SuppressWarnings(['DuplicateNumberLiteral'])
class FileUploaderTagLib {

	static namespace = 'fileuploader'
//    private static final int THOUSAND = 1000
//    private static final int ONE = 1
//    private static final int ONE_THOUSAND_TWENTY_FOUR

    def fileUploaderService

	static Long oneByte  = 1
	static Long oneKByte = 1	*	1000
	static Long oneMByte = 1 	* 	1000	*	1024
	static Long oneGByte = 1	*	1000	*	1024	*	1024

	def download = { attrs, body ->

		//checking required fields
		if (!attrs.id) {
			def errorMsg = "'id' attribute not found in file-uploader download tag."
			throwTagError(errorMsg)
		}

		if (!attrs.errorAction) {
			def errorMsg = "'errorAction' attribute not found in file-uploader form tag."
			throwTagError(errorMsg)
		}

		if (!attrs.errorController) {
			def errorMsg = "'errorController' attribute not found in file-uploader form tag."
			throwTagError(errorMsg)
		}

		params.errorAction = attrs.errorAction
		params.errorController = attrs.errorController

		out << g.link([controller: 'fileUploader', action: 'download', params: params, id: attrs.id], body)

	}

	def form = { attrs ->

		//checking required fields
		if (!attrs.upload) {
			def errorMsg = "'upload' attribute not found in file-uploader form tag."
			throwTagError(errorMsg)
		}

		if (!attrs.successAction) {
			def errorMsg = "'successAction' attribute not found in file-uploader form tag."
			throwTagError(errorMsg)
		}

		if (!attrs.successController) {
			def errorMsg = "'successController' attribute not found in file-uploader form tag."
			throwTagError(errorMsg)
		}

		if (!attrs.errorAction) {
			def errorMsg = "'errorAction' attribute not found in file-uploader form tag."
			throwTagError(errorMsg)
		}

		if (!attrs.errorController) {
			def errorMsg = "'errorController' attribute not found in file-uploader form tag."
			throwTagError(errorMsg)
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
                if (attrs.successParams) {
                    tagBody += """<input type='hidden' name='successParams'
                        value='${attrs.successParams}' />"""
                }

		//form build
		StringBuilder sb = new StringBuilder()
    sb.append g.uploadForm([controller: 'fileUploader', action: 'upload', id: attrs.id], tagBody)

		out << sb.toString()
	}

	/**
	 * @attr size REQUIRED the value to convert
	 */
	def prettysize = { attrs ->
        String sizeString = 'size'
		if (!attrs[sizeString]) {
			def errorMsg = "$sizeString attribute not found in file-uploader prettysize tag."
			throwTagError(errorMsg)
		}

		BigDecimal valSize = new BigDecimal(attrs[sizeString])

		def sb = new StringBuilder()
		if (valSize >= oneByte && valSize < oneKByte) {
			sb.append(valSize).append('b')
		} else {
            if (valSize >= oneKByte && valSize < oneMByte) {
                valSize = valSize / oneKByte
                sb.append(valSize).append('kb')
            } else {
                if (valSize >= oneMByte && valSize < oneGByte) {
                    valSize = valSize / oneMByte
                    sb.append(valSize).append('mb')
                } else {
                    if (valSize >= oneGByte) {
                        valSize = valSize / oneGByte
                        sb.append(valSize).append('gb')
                    }
                }
            }
        }

		out << sb.toString()
	}

    def resolvePath = { attrs ->
        if (!attrs.id && !attrs.instance) {
            log.error 'No Ufile instance found to resolve path for tag fileuploader:resolvePath'
            return
        }
        UFile ufileInstance = attrs.instance ?: UFile.get(attrs.id)
        out << fileUploaderService.resolvePath(ufileInstance)
    }

    def img = { attrs ->
        if (!attrs.id && !attrs.instance) {
            log.error 'No Ufile instance found to resolve path for tag fileuploader:img'
            return
        }
        UFile ufileInstance = attrs.remove('instance') ?: UFile.get(attrs.remove('id'))
        attrs.uri = fileUploaderService.resolvePath(ufileInstance)

        out << r.img(attrs)
    }
}
