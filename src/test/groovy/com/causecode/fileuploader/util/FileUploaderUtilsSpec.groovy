/*
 * Copyright (c) 2016, CauseCode Technologies Pvt Ltd, India.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or
 * without modification, are not permitted.
 */
package com.causecode.fileuploader.util

import org.springframework.web.multipart.MultipartFile
import spock.lang.Specification

/**
 * This class contains test cases for {@link FileUploaderUtils}.
 *
 * @author Hardik Modha
 * @since 3.1.3
 */
@SuppressWarnings('JavaIoPackageAccess')
class FileUploaderUtilsSpec extends Specification {

    void "test getNewTemporaryDirectoryPath method when not exception is thrown"() {
        when: 'getNewTemporaryDirectoryPath method is called'
        String tempDirectoryPath = FileUploaderUtils.newTemporaryDirectoryPath

        then: 'No exception must be thrown and a temporary path should be returned'
        noExceptionThrown()
        File tempDirectory = new File(tempDirectoryPath)
        tempDirectory.exists()

        cleanup:
        tempDirectory.deleteDir()
    }

    void "test getTempFilePathForMultipartFile when no exception is thrown"() {
        when: 'getTempFilePathForMultipartFile method is called'
        File tempFile = FileUploaderUtils.getTempFilePathForMultipartFile('Foo', '.pdf')

        then: 'No exception should be thrown'
        noExceptionThrown()
        tempFile
    }

    @SuppressWarnings('JavaIoPackageAccess')
    void "test moveFile"() {
        setup: 'Dummy Dir'
        File dir = new File('./dummyDir')
        dir.mkdir()
        dir.deleteOnExit()

        when: 'instance is of simpleFile'
        File testFile = new File('./test')
        testFile.deleteOnExit()
        FileUploaderUtils.moveFile(testFile, dir.absolutePath)

        then: 'No exception should be thrown'
        noExceptionThrown()
        dir.exists()

        when: 'instance is of multipart file'
        MultipartFile multipartFile = Mock(MultipartFile)
        multipartFile.transferTo(_) >> { File input -> }
        FileUploaderUtils.moveFile(multipartFile, dir.absolutePath)

        then: 'No exception should be thrown'
        noExceptionThrown()
        dir.exists()
    }
}
