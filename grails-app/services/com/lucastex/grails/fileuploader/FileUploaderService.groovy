package com.lucastex.grails.fileuploader

import org.codehaus.groovy.grails.commons.ConfigurationHolder
import org.springframework.web.multipart.MultipartFile

class FileUploaderService {

  def messageSource

  boolean transactional = true

/**
 * Función que sube un archivo al servidor
 * @param group El grupo del archivo
 * @param file El archivo a subir
 * @param name El nombre con el que se guardará el archivo
 * @param Locale El locale en el cual se desean los mensajes de error.
 * @return La entidad UFile que modela el archivo
 */
  def UFile saveFile(String group,def file, String name, Locale locale) throws FileUploaderServiceException {

    //config handler
    def config = ConfigurationHolder.config.fileuploader[group]
    
    //Check if file is empty
    if(file instanceof File) {
      if(!file.exists())
        return null
    } else if(file==null || file.isEmpty()){
        return null;
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
    
    fileExtension = fileName.substring(fileName.lastIndexOf('.') + 1)
    fileExtension = fileExtension.toLowerCase()
    
    if (!config.allowedExtensions[0].equals("*") && !config.allowedExtensions.contains(fileExtension)) {
      def msg = messageSource.getMessage("fileupload.upload.unauthorizedExtension", [fileExtension, config.allowedExtensions] as Object[], locale)
      log.debug msg
      throw new FileUploaderServiceException(msg)
    }

    /*********************
     check file size
     ********************* */
    def fileSize
    if (config.maxSize) { //if maxSize config exists
      if(file instanceof File)
        fileSize = file.length() / 1024
      else
        fileSize = file.size
      def maxSizeInKb = ((int) (config.maxSize / 1024))
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

    //sets new path
    def currentTime = System.currentTimeMillis()
    path = path+currentTime+"/"
    if (!new File(path).mkdirs())
            log.error "FileUploader plugin couldn't create directories: [${path}]"
    path = path + name + "." + fileExtension
    
    //move file
    log.debug "FileUploader plugin received a ${fileSize}Kb file. Moving to ${path}"
    if(file instanceof File)
      file.renameTo(new File(path))
    else
      file.transferTo(new File(path))

    //save it on the database
    def ufile = new UFile()
    ufile.name = name
    ufile.size = fileSize * 1024
    ufile.extension = fileExtension
    ufile.dateUploaded = new Date()
    ufile.path = path
    ufile.downloads = 0

    return ufile.save()
  }
  
  def boolean deleteFile(def idUfile) {
    def borro = false;
    def ufile = UFile.get(idUfile)
      if (!ufile) {
        log.error "could not delete file: ${ufile?.path}"
        return;
      }
    def file = new File(ufile.path)
    if (file.exists()) {
      if (file.delete()) {
        log.debug "file [${ufile.path}] deleted"
        def timestampFolder = ufile.path.substring(0,ufile.path.lastIndexOf("/"))
        new File(timestampFolder).delete()
        borro=true;
      } else {
       log.error "could not delete file: ${file}"
      }
    }
    return borro;
    }
}
