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
	 * @return UFile that was successfully saved
	 */
	UFile saveFile(String bucket, MultipartFile file, String name, Locale locale) throws LocalUploadServiceException {

		//config handler
		def config = grailsApplication.config.localUpload[bucket]
		
		//Check if file is empty
		if(!file || file.isEmpty()){
			def msg = messageSource.getMessage("localupload.upload.nofile", null, locale)
			log.info msg
			throw new LocalUploadServiceException(msg)
		}

		/** *********************
		 check extensions
		 *********************** */
		String fileExtension
		String fileName = file.originalFilename
		
		int extensionAt = fileName?.lastIndexOf('.') + 1
		if(extensionAt >= 0){
			fileExtension = fileName.substring(extensionAt)?.toLowerCase()
		}else{
			fileExtension = ''
		}
		
		if (!config.allowedExtensions[0].equals("*") && !config.allowedExtensions.contains(fileExtension)) {
			def msg = messageSource.getMessage("localupload.upload.unauthorizedExtension", [fileExtension, config.allowedExtensions] as Object[], locale)
			log.debug msg
			throw new LocalUploadServiceException(msg)
		}

		/*********************
		 check file size
		 ********************* */
		def fileSize = file.size
		
		if (config.maxSize) { //if maxSize config exists	
			def maxSizeInKb = ((int) (config.maxSize)) / 1024
			if (fileSize > config.maxSize) { //if filesize is bigger than allowed
				String prettySize = FileSizeUtils.prettySizeFromBytes(fileSize)
				log.debug "LocalUpload plugin received a file bigger than allowed. Max file size is ${maxSizeInKb} kb.  Size was ${prettySize}"
				def msg = messageSource.getMessage("localupload.upload.fileBiggerThanAllowed", [maxSizeInKb, prettySize] as Object[], locale)
				throw new LocalUploadServiceException(msg)
			}
		}

		//base path to save file
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
				log.debug "Created LocalUpload plugin storage directory [${path}]"
			}else{
				log.error "LocalUpload plugin couldn't create directories: [${path}]"
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
		
		//move file
		log.debug "LocalUpload plugin received a file of size ${fileSize}. Moving to ${path}"
		file.transferTo(new File(path))

		//save it on the database
		def ufile = new UFile()
		ufile.name = (name ?: fileName)
		ufile.sizeInBytes = fileSize 
		ufile.extension = fileExtension
		ufile.dateUploaded = new Date()
		ufile.path = path
		ufile.downloads = 0
		
		if(!ufile.save()){
			log.error(errorsToString(ufile))
		}
		
		return ufile
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
				log.debug "file [${ufile.path}] deleted"
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
	UFile ufileById(Serializable idUfile, Locale locale){
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
	File fileForUFile(UFile ufile, Locale locale){
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
	private String errorsToString(obj){
		
		StringBuilder sb = new StringBuilder()
		
		obj.errors.allErrors.eachWithIndex {ObjectError error, Integer i ->
				sb.append("Error ${i+1}: ")
				
				sb.append(messageSource.getMessage(error, Locale.default))
				
				sb.append("\n")
			}
		
		return sb.toString()
	}
}
