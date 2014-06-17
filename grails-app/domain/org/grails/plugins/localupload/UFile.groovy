package org.grails.plugins.localupload


/**
 * Stores metadata about the underlying file on the file system
 *
 */
class UFile {

	/**
	 * Size of the file in bytes
	 */
	Long sizeInBytes
	
	/**
	 * Full file system path to the file
	 */
	String path
	
	/**
	 * Filename and extension
	 */
	String name
	
	/**
	 * File extension which suggests the Mime Type
	 */
	String extension
	
	/**
	 * MimeType that was detected on upload
	 */
	String mimeType
	
	/**
	 * Date the file was uploaded to the file system
	 */
	Date dateUploaded
	
	/**
	 * Number of times the file has been pulled from the file system
	 */
	Integer downloads
	
	/**
	 * Full URL to the thumbnail of this file
	 * 
	 * Currently this is not created by the LocalUpload Service, but you could create
	 * your own service to search for null values in this column, and then create the 
	 * thumbnail as a batch process.
	 */
	String pathToThumbnail
	
	static constraints = {
        sizeInBytes(min:0L)
        path()
        name()
        extension()
		mimeType nullable:true
        dateUploaded()
        downloads()
		pathToThumbnail nullable:true
	}

	String toString(){
		return path
	}
}