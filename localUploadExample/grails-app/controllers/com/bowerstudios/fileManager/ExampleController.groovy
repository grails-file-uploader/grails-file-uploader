package com.bowerstudios.fileManager

import org.grails.plugins.localupload.LocalUploadService
import org.grails.plugins.localupload.LocalUploadServiceException
import org.grails.plugins.localupload.UFile
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.multipart.MultipartHttpServletRequest

class ExampleController {

    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

	LocalUploadService localUploadService
	
    def index() {
        redirect(action: "list", params: params)
    }

    def list(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        [exampleInstanceList: Example.list(params), exampleInstanceTotal: Example.count()]
    }
	
    def create() {
        [exampleInstance: new Example(params)]
    }

	/**
	 * Remember to save the domain object afterward with example.save()
	 * @param example domain object
	 */
	private List<UFile> lookForNewAttachments(Example example){
		List<UFile> invalidFiles = []
		
		localUploadService.lookForNewAttachments(request, params, flash){ UFile ufile ->
			if(!example.files){
				example.files = []
			}
			if(ufile.hasErrors()){
				 invalidFiles << ufile
			}else{
				example.files << ufile
			}
		}
		
		return invalidFiles
	}
	
    def save() {
        def exampleInstance = new Example(params)
		
		List<UFile> invalidFiles = lookForNewAttachments(exampleInstance)
		
        if (!exampleInstance.save(flush: true)) {
            render(view: "create", model: [exampleInstance: exampleInstance])
            return
        }

        flash.message = message(code: 'default.created.message', args: [message(code: 'example.label', default: 'Example'), exampleInstance.id])
		
		checkForInvalidFilesAfterSave(invalidFiles, flash)
		
        redirect(action: "show", id: exampleInstance.id)
    }
	
	void checkForInvalidFilesAfterSave(invalidFiles, flash){
		
		if(invalidFiles){
			StringBuilder sb = new StringBuilder()
			sb.append("Saved example, but found errors with file attachment/s:  ")
			invalidFiles.each{ UFile invalidFile ->
				sb.append(invalidFile.name)
				sb.append(' ')
				sb.append(localUploadService.errorsToString(invalidFile, request.locale))
			}
			flash.message = sb.toString()
		}
	}

    def show(Long id) {
        def exampleInstance = Example.get(id)
        if (!exampleInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'example.label', default: 'Example'), id])
            redirect(action: "list")
            return
        }

        [exampleInstance: exampleInstance]
    }

    def edit(Long id) {
        def exampleInstance = Example.get(id)
        if (!exampleInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'example.label', default: 'Example'), id])
            redirect(action: "list")
            return
        }

        [exampleInstance: exampleInstance]
    }

    def update(Long id, Long version) {
        def exampleInstance = Example.get(id)
        if (!exampleInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'example.label', default: 'Example'), id])
            redirect(action: "list")
            return
        }

        if (version != null) {
            if (exampleInstance.version > version) {
                exampleInstance.errors.rejectValue("version", "default.optimistic.locking.failure",
                          [message(code: 'example.label', default: 'Example')] as Object[],
                          "Another user has updated this Example while you were editing")
                render(view: "edit", model: [exampleInstance: exampleInstance])
                return
            }
        }

        exampleInstance.properties = params
		
		List<UFile> invalidFiles = lookForNewAttachments(exampleInstance)

        if (!exampleInstance.save(flush: true)) {
            render(view: "edit", model: [exampleInstance: exampleInstance])
            return
        }

        flash.message = message(code: 'default.updated.message', args: [message(code: 'example.label', default: 'Example'), exampleInstance.id])
		
		checkForInvalidFilesAfterSave(invalidFiles, flash)
		
		redirect(action: "show", id: exampleInstance.id)
    }

    def delete(Long id) {
        def exampleInstance = Example.get(id)
        if (!exampleInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'example.label', default: 'Example'), id])
            redirect(action: "list")
            return
        }

        try {
            exampleInstance.delete(flush: true)
            flash.message = message(code: 'default.deleted.message', args: [message(code: 'example.label', default: 'Example'), id])
            redirect(action: "list")
        }
        catch (DataIntegrityViolationException e) {
            flash.message = message(code: 'default.not.deleted.message', args: [message(code: 'example.label', default: 'Example'), id])
            redirect(action: "show", id: id)
        }
    }
}
