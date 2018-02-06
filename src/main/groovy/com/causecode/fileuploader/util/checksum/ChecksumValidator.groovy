/*
 * Copyright (c) 2011 - Present, CauseCode Technologies Pvt Ltd, India.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or
 * without modification, are not permitted.
 */
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
 * @since 3.1.0
 */
class ChecksumValidator {

    /**
     * Member variable to store the calculated hash code
     */
    private String calculatedChecksum = null
    private final ChecksumConfig checksumConfig
    private final FileGroup fileGroup

    ChecksumValidator(FileGroup fileGroup) {
        this.fileGroup = fileGroup
        this.checksumConfig = getChecksumConfig(fileGroup)
    }

    /**
     * returns if flag to calculate checksum is set or not.
     * @return boolean
     */
    boolean shouldCalculateChecksum() {
        return this.checksumConfig.calculate
    }

    /**
     * Method calculates the checksum and returns calculated checksum. This method does not recalculate
     * checksum on second call.
     * This method returns previously calculated checksum in the next subsequent calls.
     * @param file
     * @return String
     * @throws FileNotFoundException
     */
    String getChecksum(def file) throws FileNotFoundException {
        calculatedChecksum = (calculatedChecksum ?: this.getChecksumForFile(file))
        return calculatedChecksum
    }

    /**
     * Returns the String representation of the algorithm being used to calculate the checksum
     * @return String
     */
    String getAlgorithm() {
        return this.checksumConfig.algorithm.toString()
    }

    /**
     * Private method to get the ChecksumConfig object from the given fileGroup object.
     * @param fileGroup
     * @return ChecksumConfig
     */
    private static ChecksumConfig getChecksumConfig(FileGroup fileGroup) {
        Map checksumProperties = fileGroup.groupConfig.checksum
        if (!checksumProperties) {
            return new ChecksumConfig()
        }

        boolean calculate = checksumProperties.calculate ?: false
        Algorithm algorithm = checksumProperties.algorithm ?: Algorithm.MD5
        return new ChecksumConfig(calculate: calculate, algorithm: algorithm)
    }

    /**
     * This is actual heart method which generates the checksum and returns its hex string representation
     * to the calling end.
     * @param file
     * @return String
     * @throws FileNotFoundException
     */
    private String getChecksumForFile(def file) throws FileNotFoundException {
        FileInputBean fileInputBean = getFileInputBeanForFile(file)
        HashCalculator hashCalculator = new FileHashCalculator(fileInputBean, this.checksumConfig.algorithm)
        return hashCalculator.calculateHash()
    }

    /**
     * Generates the FileInputBean from the given File instance. Currently this method accepts File or
     * MultipartFile Instance
     * @param file
     * @return FileInputBean
     * @throws UnRecognizedFileTypeException
     */
    private static FileInputBean getFileInputBeanForFile(def file) throws UnRecognizedFileTypeException {
        if (file in File) {
            return new SimpleFileInputBeanImpl(file)
        } else if (file in MultipartFile) {
            return new MultipartFileInputBeanImpl(file)
        }

        throw new UnRecognizedFileTypeException("${file.class.name} is not recognized for FileInputBean")
    }
}
