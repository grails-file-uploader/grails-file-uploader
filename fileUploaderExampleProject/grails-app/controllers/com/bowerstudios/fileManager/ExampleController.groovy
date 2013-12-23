package com.bowerstudios.fileManager

import org.springframework.dao.DataIntegrityViolationException

import com.bowerstudios.fileManager.Example;
import com.lucastex.grails.fileuploader.UFile

class ExampleController {

    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

    def index() {
        redirect(action: "list", params: params)
    }

    def list(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        [exampleInstanceList: Example.list(params), exampleInstanceTotal: Example.count()]
    }
	
	def attach(){
		Example example = Example.load(params.id)
		UFile ufile = UFile.load(params.ufileId)
		
		if(ufile){
			if(example.files){
				example.files.add(ufile)
			}else{
				example.files = [ufile]
			}
		}
		example.save(failOnError:true)
		
		redirect(action: "show", params: [id:params.id])
	}

    def create() {
        [exampleInstance: new Example(params)]
    }

    def save() {
        def exampleInstance = new Example(params)
        if (!exampleInstance.save(flush: true)) {
            render(view: "create", model: [exampleInstance: exampleInstance])
            return
        }

        flash.message = message(code: 'default.created.message', args: [message(code: 'example.label', default: 'Example'), exampleInstance.id])
        redirect(action: "show", id: exampleInstance.id)
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

        if (!exampleInstance.save(flush: true)) {
            render(view: "edit", model: [exampleInstance: exampleInstance])
            return
        }

        flash.message = message(code: 'default.updated.message', args: [message(code: 'example.label', default: 'Example'), exampleInstance.id])
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
