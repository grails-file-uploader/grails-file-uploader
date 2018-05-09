/*
 * Copyright (c) 2011 - Present, CauseCode Technologies Pvt Ltd, India.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or
 * without modification, are not permitted.
 */
package com.causecode.fileuploader.util.checksum

import com.causecode.fileuploader.util.checksum.beans.FileInputBean
import groovy.util.logging.Slf4j

import javax.xml.bind.annotation.adapters.HexBinaryAdapter
import java.security.MessageDigest

/**
 * This class is the Utility Class which will be used to calculate the Hash of the File
 * This class works on the byte level layer.
 * @author Milan Savaliya
 * @since 3.1.0
 */
@Slf4j
class FileHashCalculator implements HashCalculator {

    //FileInputBean instance
    private final FileInputBean FILE_INPUT_BEAN

    /**
     * Constructs FileHashCalculator instance and throws FileNotFoundException If Input File Is null or not exists
     * This uses default MD5 Hash Calculating Algorithm
     * @param algorithm
     * @param fileInputBean
     * @throws FileNotFoundException
     */
    FileHashCalculator(FileInputBean fileInputBean) throws FileNotFoundException {
        this.FILE_INPUT_BEAN = fileInputBean
        validateInputs()
    }

    /**
     * Constructs FileHashCalculator with given Algorithm instance
     * @param fileInputBean
     * @param algorithm
     */
    FileHashCalculator(FileInputBean fileInputBean, Algorithm algorithm) {
        this(fileInputBean)
        this.algorithm = algorithm
    }

    /**
     * This Method validates inputs.
     */
    private void validateInputs() throws FileNotFoundException {
        if (!FILE_INPUT_BEAN) {
            throw new FileNotFoundException('File not found')
        }
    }

    /**
     * This method calculates the hash and returns a hex String representation of Calculated Hash.
     * @return String [ Calculated Hash ]
     */
    @Override
    String calculateHash() {
        log.info "Starting checksum calculation For File ${FILE_INPUT_BEAN.name}"
        MessageDigest messageDigest = MessageDigest.getInstance(this.algorithm.toString())
        String hexHasString = new HexBinaryAdapter().marshal(messageDigest.digest(this.FILE_INPUT_BEAN.bytes))
        log.info "Calculated Checksum is:- ${hexHasString}"

        return hexHasString
    }
}
