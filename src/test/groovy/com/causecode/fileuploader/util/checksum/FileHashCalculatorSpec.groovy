/*
 * Copyright (c) 2018, CauseCode Technologies Pvt Ltd, India.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or
 * without modification, are not permitted.
 */

package com.causecode.fileuploader.util.checksum

import com.causecode.fileuploader.util.checksum.beans.FileInputBean
import com.causecode.fileuploader.util.checksum.beans.SimpleFileInputBeanImpl
import spock.lang.Specification

/**
 * Unit Test Class for FileHashCalculator class
 * @author Milan Savaliya
 */
@SuppressWarnings(['JavaIoPackageAccess'])
class FileHashCalculatorSpec extends Specification {

    private final String tempDirPath = '/tmp/'

    void "test for constructor with file parameter"() {
        when: 'Invalid File Instance is given'
        new FileHashCalculator(getFileInputBeanInstance(FileInstanceType.NULL))

        then: 'FileNotFoundException must be thrown'
        thrown(FileNotFoundException)
    }

    void "test for constructor with file parameter and algorithm parameter"() {
        given: 'Proper File Instance'
        FileInputBean file = getFileInputBeanInstance(FileInstanceType.VALID)

        and: 'Proper Algorithm Instance'
        Algorithm algorithm = Algorithm.SHA1

        when: 'Only File instance is supplied in the constructor'
        HashCalculator hashCalculator = new FileHashCalculator(file)

        then: 'instance must be created and algorithm must be set to default MD5'
        hashCalculator && hashCalculator.algorithm == Algorithm.MD5

        when: 'Valid fileInputBean instance and algorithm instances are given'
        hashCalculator = new FileHashCalculator(file, algorithm)

        then: 'instance must be created with supplied algorithm'
        hashCalculator && hashCalculator.algorithm == algorithm
    }

    void "test calculateHash method"() {
        given: 'A valid fileInputBean instance'
        FileInputBean fileInstance = getFileInputBeanInstance(FileInstanceType.VALID)

        and: 'a proper algorithm'
        Algorithm algorithm = Algorithm.SHA1

        when: 'Instance with default algorithm created and hash is calculated'
        HashCalculator hashCalculator = new FileHashCalculator(fileInstance)
        String hash = hashCalculator.calculateHash()

        then: 'hash must be generated'
        hash && !hash.isEmpty()

        when: 'Instance with supplied algorithm is created and hash is calculated'
        hashCalculator = new FileHashCalculator(fileInstance, algorithm)
        hash = hashCalculator.calculateHash()

        then: 'hash must be generated'
        hash && !hash.isEmpty()

    }

    private FileInputBean getFileInputBeanInstance(FileInstanceType fileInstanceType) {
        if (fileInstanceType == FileInstanceType.NULL) {
            return null
        } else if (fileInstanceType == FileInstanceType.NOT_EXISTS) {
            return new SimpleFileInputBeanImpl(new File(''))
        } else if (fileInstanceType == FileInstanceType.VALID) {
            File file = new File(tempDirPath.concat(System.currentTimeMillis() as String).concat('.txt'))
            file.createNewFile()
            file.deleteOnExit()
            return new SimpleFileInputBeanImpl(file)
        }

        return null

    }
}

enum FileInstanceType {
    NULL,
    NOT_EXISTS,
    VALID,
}
