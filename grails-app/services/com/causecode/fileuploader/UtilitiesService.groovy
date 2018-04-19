package com.causecode.fileuploader

import com.causecode.fileuploader.cdn.CDNFileUploader
import com.causecode.fileuploader.util.FileUploaderUtils
import grails.core.GrailsApplication
import grails.transaction.Transactional
import org.springframework.web.multipart.MultipartFile

@Transactional
class UtilitiesService {
    GrailsApplication grailsApplication

    /**
     * This method is used for dynamically instantiating the CDNFileUploader class based on the Provider.
     *
     * @param providerName The name of the provider.
     * @return Instance of the CDNFileUploader class.
     *
     * @author Nikhil Sharma
     * @since 2.4.9
     */
    CDNFileUploader getProviderInstance(String providerName) {
        String packageName = "com.causecode.fileuploader.cdn.${providerName.toLowerCase()}."
        String classNamePrefix = providerName.toLowerCase().capitalize()
        String providerClassName = packageName + "${classNamePrefix}CDNFileUploaderImpl"

        try {
            return grailsApplication.classLoader.loadClass(providerClassName)?.newInstance()
        } catch (ClassNotFoundException e) {
            log.debug 'Could not find Provider class', e
            throw new ProviderNotFoundException("Provider $providerName not found.", e)
        }
    }

    String getNewTemporaryDirectoryPath() {
        String tempDirectoryPath = FileUploaderUtils.baseTemporaryDirectoryPath + UUID.randomUUID().toString() + '/'
        File tempDirectory = new File(tempDirectoryPath)
        tempDirectory.mkdirs()

        // Delete the temporary directory when JVM exited
        tempDirectory.deleteOnExit()

        return tempDirectoryPath
    }

    File getTempFilePathForMultipartFile(String fileName, String fileExtension) {
        return new File(newTemporaryDirectoryPath + "${fileName}.${fileExtension}")
    }

    /**
     * Method is used to move file from temp directory to another.
     * @params fileInstance , path
     *
     */
    void moveFile(def file, String path) {
        if (file instanceof File) {
            file.renameTo(new File(path))
        } else {
            if (file instanceof MultipartFile) {
                file.transferTo(new File(path))
            }
        }
    }
}
