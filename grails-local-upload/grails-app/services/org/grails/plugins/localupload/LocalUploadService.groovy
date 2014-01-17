package org.grails.plugins.localupload

import groovy.io.FileType

import java.nio.channels.FileChannel

import org.codehaus.groovy.grails.commons.GrailsApplication
import org.springframework.context.MessageSource
import org.springframework.transaction.annotation.Transactional
import org.springframework.validation.ObjectError
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.multipart.MultipartHttpServletRequest

@Transactional
class LocalUploadService {

	MessageSource messageSource

	GrailsApplication grailsApplication
	
	/**
	 * Primary save method that will store a file to a bucket.  
	 * 
	 * @param bucket LocalUpload bucket in which the file is stored.
	 * @param file MultipartFile
	 * @param name Desired name for the file, defaults to submitted file name
	 * @param Locale Locale for the current request
	 * @return UFile that was created, may have validation errors
	 */
	UFile saveFile(String bucket, MultipartFile file, String name, Locale locale) throws LocalUploadServiceException {

		//config handler
		def config = grailsApplication.config.localUpload[bucket]
		
		UFile ufile = new UFile()
		
		//Check if file is empty
		if(!file || file.isEmpty()){
			ufile.errors.rejectValue("localupload.upload.nofile", "No file attached, or file is Empty")
			throw new LocalUploadServiceException(errorsToString(ufile, locale))
		}

		/***********************
		 * check extensions and mimeType
		 ************************/
		String fileExtension
		String fileName = file.originalFilename
		
		ufile.name = (name ?: fileName)
		ufile.path = buildPath(config, name, fileName)
		
		int extensionAt = fileName?.lastIndexOf('.') + 1
		if(extensionAt >= 0){
			fileExtension = fileName.substring(extensionAt)?.toLowerCase()
		}else{
			fileExtension = ''
		}
		
		// :TODO it would be a good idea to verify that the file extension matches
		// the content type, so endpoints can't sneak banned content types into our bucket.
		ufile.extension = fileExtension
		
		if (!config.allowedExtensions[0].equals("*") && 
				!config.allowedExtensions.contains(fileExtension)) {
			ufile.errors.rejectValue("extension","localupload.upload.unauthorizedExtension", 
					[fileExtension, config.allowedExtensions] as Object[], "File type not permitted")
		}
		
		ufile.mimeType = file.contentType

		/*********************
		 check file size
		 ********************* */
		Long fileSize = file.size
		Long maxSize = config.maxSize
		if (maxSize) { //if maxSize config exists
			if (fileSize > maxSize) { //if filesize is bigger than allowed
				
				String prettyFileSize = FileSizeUtils.prettySizeFromBytes(fileSize)
				String prettyMaxSize = FileSizeUtils.prettySizeFromBytes(maxSize)
				
				ufile.errors.rejectValue("sizeInBytes", "localupload.upload.fileBiggerThanAllowed", 
						[prettyMaxSize, prettyFileSize] as Object[], "File size too large.")
			}
		}
		
		ufile.sizeInBytes = fileSize 
		
		ufile.dateUploaded = new Date()
		ufile.downloads = 0
	
		//Validate before we attempt to persist the file to disk:  WARNING, does not look at domain class constraints until save, because we haven't called validate()
		if(!ufile.hasErrors()){
			log.debug "LocalUpload plugin received a file of size ${fileSize}. Moving to ${ufile.path}"
			try{
				file.transferTo(new File(ufile.path))
			}catch(Exception e){
				log.error("Failed to persist file to disk", e)
				ufile.errors.rejectValue("path", 
						"localupload.upload.persistenceError", "Failed to save file" )
			}
			
			if(!ufile.hasErrors()){
				//save it on the database
				ufile.save()
			}
		}
		
		return ufile
	}
	
	protected String buildPath(config, name, fileName){
		def path = config.path
		if (!path.endsWith('/') || !path.endsWith(File.separator))
			path = path + File.separator

		//setup storage path
		def storageTypes = config.storageTypes
	
		if(storageTypes?.contains('monthSubdirs')){  //subdirectories by month and year
			Calendar cal = Calendar.getInstance()
			path = path + cal[Calendar.YEAR].toString() + (cal[Calendar.MONTH]+1).toString() + File.separator
		}else{  //subdirectories by millisecond
			long currentTime = System.currentTimeMillis()
			path = path + currentTime + File.separator
		}
		
		//make sure the directory exists
		if(! new File(path).exists() ){
			if (new File(path).mkdirs()){
				log.info "Created LocalUpload plugin storage directory [${path}]"
			}else{
				log.error "LocalUpload plugin couldn't create directories: [${path}]"
				//:TODO need to throw an exception here.
			}
		}
		
		if(storageTypes?.contains('plain')){
			//note:  this type of storage is a bit of a security / data loss risk.
			path = path + (name ?: fileName)
		}else{
			/* Using uuids as filenames, this lends us slightly more security.  If
			 * two users upload a file with the same name, the files will not
			 * overlap
			 */
			path = path + UUID.randomUUID().toString()
		}
		
		return path
	}
	
	boolean deleteFile(UFile ufile) {
		boolean borro = false;
		
		File file = new File(ufile.path)
		File parent
		
		if(file.exists()){
			parent = new File(file.parentFile)
		}
		
		try{
			ufile.delete()
			borro=true;
		}catch(Exception e){
			log.error("could not delete ufile: ${ufile.id}", e)
		}
		
		if(file.exists()) {
			if (file.delete()) {
				log.info "file [${ufile.path}] deleted"
			}else {
				log.error "could not delete file: ${file}"
			}
		}

		//after deleting the file, lets manage the parent folder
		manageFolder(file.parentFile)

		return borro;
	}

	/**
	 * check if this folder is empty, if so, delete it
	 * @param folder
	 */
	void manageFolder(File folder){
		int numFilesInParentFolder = 0
		folder.eachFile(FileType.FILES) {
			numFilesInParentFolder++
		}
		if(numFilesInParentFolder==0){
			folder.delete()
		}else{
			log.debug("not deleting ${folder} as it contains files")
		}
	}
	
	/**
	 * Access the Ufile, returning the appropriate message if the UFile does not exist.
	 */
	@Transactional(readOnly = true)
	UFile ufileById(Serializable idUfile, Locale locale) throws FileNotFoundException{
		UFile ufile = UFile.get(idUfile)
		
		if(ufile){
			return ufile
		
		}else{
			String msg = messageSource.getMessage(
					"localupload.download.nofile",
					[idUfile] as Object[], locale)
			throw new FileNotFoundException(idUfile.toString())
		}
	}
	
	/**
	 * Access the file held by the UFile, incrementing the viewed number, and returning appropriate message if file does not exist.
	 */
	File fileForUFile(UFile ufile, Locale locale) throws IOException{
		File file = new File(ufile.path)
		
		if(file.exists()){
			//increment the viewed number
			ufile.downloads++
			ufile.save()
			
			return file
			
		}else{
			String msg = messageSource.getMessage(
						"localupload.download.filenotfound",
						[ufile.name] as Object[], locale)
			
			throw new IOException(msg)
		}
	}
	
	/**
	 * Helper method for Controllers to look for new attachments, and add them 
	 * to the domain object.  Passes a UFile object to the closure.  Iterates over
	 * all files submitted, and runs the closure for each
	 * @param closure
	 * @return a list of the new UFiles
	 */
	List<UFile> lookForNewAttachments(def request, def params, def flash, Closure closure){
		String bucket = params.bucket
		String fileParam = params.fileParam ?: 'files'
		
		List<UFile> newAttachments = []
		
		if (request instanceof MultipartHttpServletRequest){
			MultipartHttpServletRequest req = request
			for(MultipartFile file in req.getFiles(fileParam)){
				if(file.empty){
					continue
				}
				
				UFile ufile
				try{
					ufile = saveFile(bucket, file, file.originalFilename, request.locale)
					
					newAttachments << ufile
					
					closure.call(ufile)
					
				}catch(LocalUploadServiceException e){
					log.error("Failed to upload file", e)
					flash.message = "Failed to upload ${file?.originalFilename}"
				}
			}
		}
		
		return newAttachments
	}

	/** 
	 * Simple helper to expose the errors on a domain object as a string
	 */
	protected String errorsToString(obj, Locale locale){
		
		StringBuilder sb = new StringBuilder()
		
		obj.errors.allErrors.eachWithIndex {ObjectError error, Integer i ->
				sb.append("Error ${i+1}: ")
				
				sb.append(messageSource.getMessage(error, locale?:Locale.default))
				
				sb.append("\n")
			}
		
		return sb.toString()
	}
}
