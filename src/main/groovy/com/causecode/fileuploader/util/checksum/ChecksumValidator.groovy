package com.causecode.fileuploader.util.checksum

import com.causecode.fileuploader.FileGroup
import com.causecode.fileuploader.util.checksum.beans.FileInputBean
import com.causecode.fileuploader.util.checksum.beans.MultipartFileInputBeanImpl
import com.causecode.fileuploader.util.checksum.beans.SimpleFileInputBeanImpl
import com.causecode.fileuploader.util.checksum.exceptions.UnRecognizedFileTypeException
import org.springframework.web.multipart.MultipartFile

/**
 * Helper class to validate and generate checksum. This class will communicate with service to
 * generate checksum
 * @author Milan Savaliya
 */
class ChecksumValidator {
    private String calculatedChecksum = null
    private ChecksumConfig checksumConfig
    private FileGroup fileGroup

    ChecksumValidator(FileGroup fileGroup){
        this.fileGroup = fileGroup
        this.checksumConfig = getChecksumConfig(fileGroup)
    }

    boolean isToCalculateChecksum() {
        return this.checksumConfig.calculate
    }

    String getChecksum(def file){
        if( calculatedChecksum == null ) {
            calculatedChecksum = this.getChecksumForFile(file)
        }
        return calculatedChecksum
    }

    String getAlgorithm(){
        return this.checksumConfig.algorithm.toString()
    }

    private ChecksumConfig getChecksumConfig(FileGroup fileGroup) {
        def checksumPro = fileGroup.groupConfig.checksum
        if (!checksumPro) return new ChecksumConfig()

        boolean calculate = checksumPro.calculate ?: false
        Algorithm algorithm = checksumPro.algorithm ?: Algorithm.MD5
        return new ChecksumConfig(calculate: calculate, algorithm: algorithm)
    }

    private String getChecksumForFile(def file) {
        FileInputBean fileInputBean = getFileInputBeanForFile(file)
        HashCalculator hashCalculator = new FileHashCalculator(fileInputBean, this.checksumConfig.algorithm)
        return hashCalculator.calculateHash()
    }


    private FileInputBean getFileInputBeanForFile(def file) {
        if (file instanceof File) {
            return new SimpleFileInputBeanImpl(file)
        } else if (file instanceof MultipartFile) {
            return new MultipartFileInputBeanImpl(file)
        } else {
            throw new UnRecognizedFileTypeException("${file.class.getName()} is not recognized for FileInputBean")
        }
    }
}
