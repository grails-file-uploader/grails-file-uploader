package com.lucastex.grails.fileuploader

import groovy.io.FileType

import java.nio.channels.FileChannel

class FileUploaderService {

    def messageSource
    def grailsApplication

    UFile saveFile(String group, def file, String name = "", Locale locale = null) throws FileUploaderServiceException {
        def config = grailsApplication.config.fileuploader[group]

        if(file instanceof File) {
            if(!file || !file.exists() || file.isEmpty()) {
                log.info "Empty or no file received"
                return null
            }
        }

        String fileName = ""
        String fileExtension = ""
        if(file instanceof File) {
            fileName = file.name
        } else {
            fileName = file.originalFilename
        }

        int extensionAt = fileName.lastIndexOf('.') + 1
        if(extensionAt >= 0) {
            fileExtension = fileName.substring(extensionAt).toLowerCase()
        }

        if (!config.allowedExtensions[0].equals("*") && !config.allowedExtensions.contains(fileExtension)) {
            def msg = messageSource.getMessage("fileupload.upload.unauthorizedExtension",
                    [fileExtension, config.allowedExtensions] as Object[], locale)
            log.debug msg
            throw new FileUploaderServiceException(msg)
        }

        /**
         * Check file size
         */
        def fileSize
        if(file instanceof File) {
            fileSize = file.size()
        } else {
            fileSize = file.size
        }

        /**
         * If maxSize config exists
         */
        if (config.maxSize) {
            def maxSizeInKb = ((int) (config.maxSize)) / 1024
            if (fileSize > config.maxSize) { //if filesize is bigger than allowed
                log.debug "FileUploader plugin received a file bigger than allowed. Max file size is ${maxSizeInKb} kb"
                def msg = messageSource.getMessage("fileupload.upload.fileBiggerThanAllowed", [maxSizeInKb] as Object[], locale)
                throw new FileUploaderServiceException(msg)
            }
        }

        // Base path to save file
        def path = config.path
        if (!path.endsWith('/'))
            path = path + "/"

        // Setup storage path
        def storageTypes = config.storageTypes

        if(storageTypes?.contains('monthSubdirs')) {  //subdirectories by month and year
            Calendar cal = Calendar.getInstance()
            path = path + cal[Calendar.YEAR].toString() + cal[Calendar.MONTH].toString() + '/'
        } else {  //subdirectories by millisecond
            long currentTime = System.currentTimeMillis()
            path = path + currentTime + "/"
        }

        // Make sure the directory exists
        if(! new File(path).exists() ){
            if (!new File(path).mkdirs()) {
                log.error "FileUploader plugin couldn't create directories: [${path}]"
            } else {
                log.debug "Created plugin storage directory [${path}]"
            }
        }

        // If using the uuid storage type
        if(storageTypes?.contains('uuid')){
            path = path + UUID.randomUUID().toString()
        } else {  //note:  this type of storage is a bit of a security / data loss risk.
            path = path + (name ? (name + "." + fileExtension) : fileName)
        }

        // Move file
        log.debug "FileUploader plugin received a ${fileSize} file. Moving to ${path}"
        if(file instanceof File)
            file.renameTo(new File(path))
        else
            file.transferTo(new File(path))

        UFile ufile = new UFile()
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
        UFile ufile = UFile.get(idUfile)
        if (!ufile) {
            log.error "could not delete file: ${ufile?.path}"
            return false
        }
        File file = new File(ufile.path)

        try {
            ufile.delete()
        } catch(Exception e) {
            log.error "Could not delete ufile: ${idUfile}", e
            return false
        }

        deleteFileForUFile(file)
        return true
    }

    boolean deleteFileForUFile(File file) {
        if(!file.exists()) {
            return false
        }
        File timestampFolder = file.parentFile

        if (file.delete()) {
            log.debug "File [${file?.path}] deleted."

            int numFilesInParentFolder = 0
            timestampFolder.eachFile(FileType.FILES) {
                numFilesInParentFolder ++
            }
            if(numFilesInParentFolder == 0) {
                timestampFolder.delete()
            } else {
                log.debug "Not deleting ${timestampFolder} as it contains files"
            }
        } else {
            log.error "Could not delete file: ${file}"
        }
    }

    /**
     * Access the Ufile, returning the appropriate message if the UFile does not exist.
     */
    UFile ufileById(Serializable idUfile, Locale locale){
        UFile ufile = UFile.get(idUfile)

        if(ufile) {
            return ufile
        }
        String msg = messageSource.getMessage("fileupload.download.nofile", [idUfile] as Object[], locale)
        throw new FileNotFoundException(msg)
    }

    /**
     * Access the file held by the UFile, incrementing the viewed number, and returning appropriate message if file does not exist.
     */
    File fileForUFile(UFile ufile, Locale locale){
        File file = new File(ufile.path)

        if(file.exists()){
            //increment the viewed number
            ufile.downloads ++
            ufile.save()
            return file
        }
        String msg = messageSource.getMessage("fileupload.download.filenotfound", [ufile.name] as Object[], locale)
        throw new IOException(msg)
    }

    /**
     * Method to create a duplicate of an existing UFile
     * @param group
     * @param uFile
     * @param name
     * @param locale
     * @throws FileUploaderServiceException
     * @throws IOException
     */
    UFile cloneFile(String group, UFile uFile, String name, Locale locale) throws FileUploaderServiceException,IOException {
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
                source.close()
            }
            if(destination) {
                destination.close()
            }

            if(destFile.exists()) {
                return this.saveFile(group, destFile, name, locale)
            }
        }
    }

}