package com.causecode.fileuploader.util.checksum

import com.causecode.fileuploader.BaseTestSetup
import com.causecode.fileuploader.FileGroup
import com.causecode.fileuploader.UFile
import com.causecode.fileuploader.util.checksum.exceptions.UnRecognizedFileTypeException
import grails.buildtestdata.mixin.Build
import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import org.grails.plugins.testing.GrailsMockMultipartFile
import spock.lang.Specification

/**
 * @author Milan Savaliya
 */
@Build(UFile)
@TestMixin(GrailsUnitTestMixin)
@SuppressWarnings(['JavaIoPackageAccess'])
class ChecksumValidatorSpec extends Specification implements BaseTestSetup {

    void "test Constructor"() {
        given: 'fileGroup instance'
        FileGroup fileGroupInstance = new FileGroup('testLocal')
        and: 'invalid checksum config'
        def checksumConfig = null
        fileGroupInstance.groupConfig.checksum = checksumConfig

        when: 'constructor is called'
        def instance = new ChecksumValidator(fileGroupInstance)
        then: 'expect valid instance'
        instance != null

        when: 'given valid checksum config object'
        fileGroupInstance.groupConfig.checksum = [calculate: true, algorithm: Algorithm.SHA1,]
        instance = new ChecksumValidator(fileGroupInstance)
        then: 'expect a valid instance'
        instance != null
    }

    void 'test isToCalculateChecksum method'() {
        given: 'fileGroup instance'
        FileGroup fileGroupInstance = new FileGroup('testLocal')
        and: 'mocked config object'
        fileGroupInstance.groupConfig.checksum = [calculate: true, algorithm: Algorithm.SHA1,]
        def instance = new ChecksumValidator(fileGroupInstance)
        expect: 'instance is ready'
        instance.isToCalculateChecksum()
    }

    void 'test getChecksum method with java.io.file instance'() {
        given: 'java.io.file instance'
        File fileInstance = new File('/tmp/testLocal.txt')
        fileInstance.createNewFile()
        fileInstance.write('Some dummy date in')
        fileInstance.deleteOnExit()

        and: 'fileGroup instance'
        FileGroup fileGroupInstance = new FileGroup('testLocal')

        when: 'getChecksum method is called'
        fileGroupInstance.groupConfig.checksum = [calculate: true, algorithm: Algorithm.SHA1,]
        def instance = new ChecksumValidator(fileGroupInstance)

        then: 'expect calculated checksum'
        def checksum = instance.getChecksum(fileInstance)
        checksum != null
        !checksum.isEmpty()
    }

    void 'test getChecksum method with MultipartFile instance'() {
        given: 'MultipartFile instance'
        def test = new GrailsMockMultipartFile('testOne', 'testOne', 'text', [1, 2, 3, 4, 5] as byte[])

        and: 'fileGroup instance'
        FileGroup fileGroupInstance = new FileGroup('testLocal')

        when: 'getChecksum method is called'
        fileGroupInstance.groupConfig.checksum = [calculate: true, algorithm: Algorithm.SHA1]
        def instance = new ChecksumValidator(fileGroupInstance)

        then: 'expect calculated checksum'
        def checksum = instance.getChecksum(test)
        checksum != null
        !checksum.isEmpty()
    }

    void 'test getAlgorithm method'() {
        given: 'fileGroup instance'
        FileGroup fileGroupInstance = new FileGroup('testLocal')

        when: 'getChecksum method is called'
        fileGroupInstance.groupConfig.checksum = [calculate: true, algorithm: Algorithm.SHA1]
        def instance = new ChecksumValidator(fileGroupInstance)
        def algorithm = instance.algorithm

        then: 'expected supplied algorithm instance'
        algorithm == Algorithm.SHA1.toString()
    }

    void 'test getFileInputBeanForFile method'() {
        given: 'Unknown instance'
        def test = new Object()

        and: 'fileGroup instance'
        FileGroup fileGroupInstance = new FileGroup('testLocal')

        when: 'getChecksum method is called'
        fileGroupInstance.groupConfig.checksum = [calculate: true, algorithm: Algorithm.SHA1]
        def instance = new ChecksumValidator(fileGroupInstance)
        instance.getChecksum(test)
        then: 'expected UnRecognizedFileTypeException'
        thrown(UnRecognizedFileTypeException)
    }
}
