package com.causecode.fileuploader.util.checksum

import com.causecode.fileuploader.util.checksum.beans.FileInputBean
import com.causecode.fileuploader.util.checksum.beans.SimpleFileInputBeanImpl
import spock.lang.Specification
import spock.lang.Unroll

/**
 * See the API for {@link grails.test.mixin.support.GrailsUnitTestMixin} for usage instructions
 */
class FileHashCalculatorSpec extends Specification {

    private final String tempDirPath = '/tmp/'

    def setup() {

    }

    def cleanup() {
    }

    @Unroll
    void 'test for constructor with file parameter'() {
        when: 'Invalid File Instance is given'
        def hashCalcy = new FileHashCalculator(getFileInstance(FileInstanceType.NULL))
        then: 'FileNotFoundException must be thrown'
        thrown(FileNotFoundException)
    }

    void 'test for constructor with file parameter and algorithm pramter'() {
        given: 'Proper File Instance'
        def file = getFileInstance(FileInstanceType.VALID)
        and: 'Proper Algorithm Instance'
        def algorithm = Algorithm.SHA1

        when: 'Only File instance is supplied in the constructor'
        def hashCalcy = new FileHashCalculator(file)
        then: 'instance must be created and algorithm must be set to default MD5'
        hashCalcy != null
        hashCalcy.algorithm == Algorithm.MD5

        when: 'Valid fileInputBean instance and algorithm instances are given'
        hashCalcy = new FileHashCalculator(file, algorithm)
        then: 'instance must be created with supplied algorithm'
        hashCalcy != null
        hashCalcy.algorithm == algorithm
    }

    void 'test calculateHash method'() {
        given: 'A valid fileInputBean instance'
        def fileInstance = getFileInstance(FileInstanceType.VALID)
        and: 'a proper algorithm'
        def algorithm = Algorithm.SHA1

        when: 'Instance with default algorithm created'
        def hashCalculator = new FileHashCalculator(fileInstance)
        then: 'hash must be generated'
        hashCalculator.calculateHash() != null

        when: 'Instance with supplied algorithm is created'
        hashCalculator = new FileHashCalculator(fileInstance, algorithm)
        then: 'hash must be generated'
        hashCalculator.calculateHash() != null

    }

    private FileInputBean getFileInstance(FileInstanceType fileInstanceType) {
        if (fileInstanceType == FileInstanceType.NULL) {
            return null
        } else if (fileInstanceType == FileInstanceType.NOT_EXISTS) {
            return new SimpleFileInputBeanImpl(new File(''))
        } else if (fileInstanceType == FileInstanceType.VALID) {
            def file = new File(tempDirPath.concat(System.currentTimeMillis() as String).concat(".txt"))
            file.createNewFile()
            file.deleteOnExit()
            return new SimpleFileInputBeanImpl(file)
        } else {
            return null
        }

    }
}

enum FileInstanceType {
    NULL,
    NOT_EXISTS,
    VALID,
}