package org.grails.plugins.localupload

import groovy.io.FileType

import java.nio.channels.FileChannel

import org.codehaus.groovy.grails.commons.GrailsApplication
import org.springframework.context.MessageSource
import org.springframework.transaction.annotation.Transactional

@Transactional
class LocalUploadService {

	MessageSource messageSource

	GrailsApplication grailsApplication

	/**
	 * Primary save method that will store a file to a bucket.  
	 * 
	 * @param bucket LocalUpload bucket in which the file is stored.
	 * @param file Either a java.io.File or a org.springframework.web.multipart.MultipartFile
	 * @param name Desired name for the file, defaults to submitted file name
	 * @param Locale Locale for the current request
	 * @return UFile that was successfully saved
	 */
	UFile saveFile(String bucket, def file, String name, Locale locale) throws LocalUploadServiceException {

		//config handler
		def config = grailsApplication.config.localUpload[bucket]
		
		//Check if file is empty
		if(file instanceof File) {
			if(!file || !file.exists() || file.isEmpty()){
				def msg = messageSource.getMessage("fileupload.upload.nofile", null, locale)
				log.debug msg
				throw new LocalUploadServiceException(msg)
			}
		}

		/** *********************
		 check extensions
		 *********************** */
		String fileExtension
		String fileName
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
			throw new LocalUploadServiceException(msg)
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
				log.debug "LocalUpload plugin received a file bigger than allowed. Max file size is ${maxSizeInKb} kb"
				def msg = messageSource.getMessage("fileupload.upload.fileBiggerThanAllowed", [maxSizeInKb] as Object[], locale)
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
			path = path + cal[Calendar.YEAR].toString() + cal[Calendar.MONTH].toString() + File.separator
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
			path = path + (name ? (name + "." + fileExtension) : fileName)
		}else{  
			/* Using uuids as filenames, this lends us slightly more security.  If 
			 * two users upload a file with the same name, the files will not 
			 * overlap
			 */
			path = path + UUID.randomUUID().toString()
		}
		
		//move file
		log.debug "LocalUpload plugin received a ${fileSize} file. Moving to ${path}"
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
	 * @param bucket
	 * @param uFile
	 * @param name
	 * @param locale
	 * @return
	 * @throws LocalUploadServiceException
	 * @throws IOException
	 */
	UFile cloneFile(String bucket, UFile uFile, String name, Locale locale)
			throws LocalUploadServiceException, IOException {
		
		//Create temp directory
		def tempDirectory = "./web-app${File.separator}temp${File.separator}${System.currentTimeMillis()}${File.separator}"
		new File(tempDirectory).mkdirs()
	
		//create file
		def tempFile = "${tempDirectory}${File.separator}${uFile.name}.${uFile.extension}"
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
				return this.saveFile(bucket, destFile,name, locale)
			}
		}
	}
}
