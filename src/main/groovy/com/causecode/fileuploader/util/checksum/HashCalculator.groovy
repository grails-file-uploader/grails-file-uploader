/*
 * Copyright (c) 2011 - Present, CauseCode Technologies Pvt Ltd, India.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or
 * without modification, are not permitted.
 */
package com.causecode.fileuploader.util.checksum

/**
 * Trait Specifying Hash Calculating Behaviour
 * @author Milan Savaliya
 * @since 3.1.0
 */
trait HashCalculator {

    /**
     * Instance of an Algorithm which would be used to calculate the hashF
     * Default is set to the "MD5"
     */
    private Algorithm algorithm = Algorithm.MD5

    /**
     * Setter for algorithm
     */
    void setAlgorithm(Algorithm algorithm) {
        this.algorithm = algorithm
    }

    /**
     * This method returns an instance of a algorithm which will be used to calculate the checksum
     * @return
     */
    Algorithm getAlgorithm() {
        return this.algorithm
    }

    /**
     * This method returns an String Representation of the Calculated Hash
     * @return
     */
    abstract String calculateHash()
}
