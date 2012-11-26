package com.lucastex.grails.fileuploader

import groovy.io.FileType

import java.nio.channels.FileChannel

import org.codehaus.groovy.grails.commons.GrailsApplication
import org.springframework.context.MessageSource

class FileUploaderService {

	MessageSource messageSource

	GrailsApplication grailsApplication
	
	boolean transactional = true

	/**
	 * Función que sube un archivo al servidor
	 * @param group El grupo del archivo
	 * @param file El archivo a subir
	 * @param name El nombre con el que se guardará el archivo
	 * @param Locale El locale en el cual se desean los mensajes de error.
	 * @return La entidad UFile que modela el archivo
	 */
	UFile saveFile(String group, def file, String name, Locale locale) throws FileUploaderServiceException {

		//config handler
		def config = grailsApplication.config.fileuploader[group]
		
		//Check if file is empty
		if(file instanceof File) {
			if(!file || !file.exists() || file.isEmpty()){
				def msg = messageSource.getMessage("fileupload.upload.nofile", null, locale)
				log.debug msg
				throw new FileUploaderServiceException(msg)
			}
		}

		/** *********************
		 check extensions
		 *********************** */
		def fileExtension
		def fileName
		if(file instanceof File) {
			fileName = file.name
		} else {
			fileName = file.originalFilename
		}
		
		int extensionAt = fileName?.lastIndexOf('.') + 1
		if(extensionAt >= 0){
			fileExtension = fileName.substring(extensionAt)?.toLowerCase()
		}else{
			fileExtension = ''
		}
		
		if (!config.allowedExtensions[0].equals("*") && !config.allowedExtensions.contains(fileExtension)) {
			def msg = messageSource.getMessage("fileupload.upload.unauthorizedExtension", [fileExtension, config.allowedExtensions] as Object[], locale)
			log.debug msg
			throw new FileUploaderServiceException(msg)
		}

		/*********************
		 check file size
		 ********************* */
		def fileSize
		if(file instanceof File){
			fileSize = file.size()
		}else{
			fileSize = file.size
		}
		
		if (config.maxSize) { //if maxSize config exists	
			def maxSizeInKb = ((int) (config.maxSize)) / 1024
			if (fileSize > config.maxSize) { //if filesize is bigger than allowed
				log.debug "FileUploader plugin received a file bigger than allowed. Max file size is ${maxSizeInKb} kb"
				def msg = messageSource.getMessage("fileupload.upload.fileBiggerThanAllowed", [maxSizeInKb] as Object[], locale)
				throw new FileUploaderServiceException(msg)
			}
		}

		//base path to save file
		def path = config.path
		if (!path.endsWith('/'))
			path = path + "/"

		//setup storage path
		def storageTypes = config.storageTypes
	
		if(storageTypes?.contains('monthSubdirs')){  //subdirectories by month and year
			Calendar cal = Calendar.getInstance()
			path = path + cal[Calendar.YEAR].toString() + cal[Calendar.MONTH].toString() + '/'
		}else{  //subdirectories by millisecond
			long currentTime = System.currentTimeMillis()
			path = path + currentTime + "/"
		}
		
		//make sure the directory exists
		if(! new File(path).exists() ){
			if (!new File(path).mkdirs()){
				log.error "FileUploader plugin couldn't create directories: [${path}]"
			}else{
				log.debug "Created plugin storage directory [${path}]"
			}
		}
		
		//If using the uuid storage type
		if(storageTypes?.contains('uuid')){
			path = path + UUID.randomUUID().toString()
		}else{  //note:  this type of storage is a bit of a security / data loss risk.
			path = path + (name ? (name + "." + fileExtension) : fileName)
		}
		
		//move file
		log.debug "FileUploader plugin received a ${fileSize} file. Moving to ${path}"
		if(file instanceof File)
			file.renameTo(new File(path))
		else
			file.transferTo(new File(path))

		//save it on the database
		def ufile = new UFile()
		ufile.name = (name ? (name + "." + fileExtension) : fileName)
		ufile.size = fileSize 
		ufile.extension = fileExtension
		ufile.dateUploaded = new Date()
		ufile.path = path
		ufile.downloads = 0
		ufile.save()
		return ufile
	}

	
	boolean deleteFile(Serializable idUfile) {
		boolean borro = false;
		UFile ufile = UFile.get(idUfile)
			if (!ufile) {
				log.error "could not delete file: ${ufile?.path}"
				return;
			}
		File file = new File(ufile.path)
		
		try{
			ufile.delete()
			borro=true;
		}catch(Exception e){
			log.error("could not delete ufile: ${idUfile}", e)
		}
		
		if(file.exists()) {
			File timestampFolder = file.parentFile
			
			if (file.delete()) {
				log.debug "file [${ufile.path}] deleted"
				
				int numFilesInParentFolder = 0
				timestampFolder.eachFile(FileType.FILES) {
					numFilesInParentFolder++
				}
				if(numFilesInParentFolder==0){
					timestampFolder.delete()
				}else{
					log.debug("not deleting ${timestampFolder} as it contains files")
				}
				
			}else {
				log.error "could not delete file: ${file}"
			}
		}
		return borro;
		}

	/**
	 * Access the Ufile, returning the appropriate message if the UFile does not exist.
	 */
	UFile ufileById(Serializable idUfile, Locale locale){
		UFile ufile = UFile.get(idUfile)
		
		if(ufile){
			return ufile
		
		}else{
			String msg = messageSource.getMessage(
					"fileupload.download.nofile",
					[idUfile] as Object[], locale)
			throw new FileNotFoundException(idUfile.toString())
			
		}
	}
	
	/**
	 * Access the file held by the UFile, incrementing the viewed number, and returning appropriate message if file does not exist.
	 */
	File fileForUFile(UFile ufile, Locale locale){
		File file = new File(ufile.path)
		
		if(file.exists()){
			//increment the viewed number
			ufile.downloads++
			ufile.save()
			
			return file
			
		}else{
			String msg = messageSource.getMessage(
						"fileupload.download.filenotfound",
						[ufile.name] as Object[], locale)
			
			throw new IOException(msg)
		}
	}
	
	/**
	 * Method to create a duplicate of an existing UFile
	 * @param group
	 * @param uFile
	 * @param name
	 * @param locale
	 * @return
	 * @throws FileUploaderServiceException
	 * @throws IOException
	 */
	UFile cloneFile(String group, UFile uFile, String name, Locale locale)
			throws FileUploaderServiceException,IOException {
		
		//Create temp directory
		def tempDirectory = "./web-app/temp/${System.currentTimeMillis()}/"
		new File(tempDirectory).mkdirs()
	
		//create file
		def tempFile = "${tempDirectory}/${uFile.name}.${uFile.extension}"
		def destFile = new File(tempFile)
		def sourceFile = new File(uFile.path)
		if(!destFile.exists()) {
			destFile.createNewFile();
		}

		FileChannel source = null;
		FileChannel destination = null;

		try {
			source = new FileInputStream(sourceFile).getChannel();
			destination = new FileOutputStream(destFile).getChannel();
			destination.transferFrom(source, 0, source.size());
		} finally {
			if(source) {
				source.close();
			}
			if(destination) {
				destination.close();
			}

			if(destFile.exists()) {
				return this.saveFile(group, destFile,name, locale)
			}
		}
	}
}
