package com.causecode.fileuploader.util.checksum

import com.causecode.fileuploader.util.checksum.beans.FileInputBean
import groovy.util.logging.Slf4j

import javax.xml.bind.annotation.adapters.HexBinaryAdapter
import java.security.MessageDigest

/**
 * This class is the Utility Class which will be used to calculate the Hash of the File
 * This class works on the byte level layer.
 * @author Milan Savaliya
 */
@Slf4j
class FileHashCalculator implements HashCalculator {

    /**
     * FileInputBean instance
     */
    private FileInputBean fileInputBean

    /**
     * Constructs FileHashCalculator instance and throws FileNotFoundException If Input File Is null or not exists
     * This uses default MD5 Hash Calculating Algorithm
     * @param algorithm
     * @param fileInputBean
     * @throws FileNotFoundException
     */
    FileHashCalculator(FileInputBean fileInputBean) throws FileNotFoundException {
        this.fileInputBean = fileInputBean
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
        if (fileInputBean == null ) throw new FileNotFoundException("File not found with")
    }

    /**
     * This method calculates the hash and returns a hexadecimal String representation of Calculated Hash.
     * @return String [ Calculated Hash ]
     */
    @Override
    String calculateHash() {
        log.info "Starting checksum calculation For File ${fileInputBean.getName()}"
        def messageDigest = MessageDigest.getInstance(this.algorithm.toString())
        def hexHasString = new HexBinaryAdapter().marshal(messageDigest.digest(this.fileInputBean.getBytes()))
        log.info "Calculated Checksum is:- ${hexHasString}"
        return hexHasString
    }
}
