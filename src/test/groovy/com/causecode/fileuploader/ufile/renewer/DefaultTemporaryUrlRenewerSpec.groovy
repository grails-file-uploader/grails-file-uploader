/*
 * Copyright (c) 2017-Present, CauseCode Technologies Pvt Ltd, India.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or
 * without modification, are not permitted.
 */
package com.causecode.fileuploader.ufile.renewer

import com.causecode.fileuploader.CDNProvider
import com.causecode.fileuploader.StorageException
import com.causecode.fileuploader.UFile
import com.causecode.fileuploader.UFileType
import com.causecode.fileuploader.cdn.CDNFileUploader
import com.causecode.fileuploader.cdn.google.GoogleCDNFileUploaderImpl
import com.causecode.util.NucleusUtils
import grails.buildtestdata.mixin.Build
import grails.util.Holders
import groovy.json.JsonBuilder
import spock.lang.Specification
import spock.lang.Unroll

/**
 * Unit tests for {@link DefaultTemporaryUrlRenewer}
 *
 * @author Milan Savaliya
 * @author Hardik Modha
 * @since 3.1.3
 */
@Build([UFile])
class DefaultTemporaryUrlRenewerSpec extends Specification {

    UFile createUFileInstance(Date expirationDate) {
        return UFile.build(
                downloads: 100,
                provider: CDNProvider.GOOGLE,
                size: 100,
                path: new Date().time.toString(),
                name: new Date().time.toString(),
                fileGroup: 'SomeFileGroup',
                type: UFileType.CDN_PUBLIC,
                expiresOn: expirationDate
        )
    }

    DefaultTemporaryUrlRenewer getDefaultTemporaryUrlRenewerInstance(boolean forceAll = true,
            int maxInOneIteration = 5, CDNFileUploader cdnFileUploader = null) {

        CDNFileUploader tempCDNFileUploader = cdnFileUploader ?: Mock(GoogleCDNFileUploaderImpl)

        return new DefaultTemporaryUrlRenewer(
                CDNProvider.GOOGLE,
                tempCDNFileUploader,
                forceAll,
                maxInOneIteration
        )
    }

    @Unroll
    void "test constructor when inputs are not valid"() {
        when: 'DefaultTemporaryUrlRenewer instance is created'
        DefaultTemporaryUrlRenewer.newInstance(
                cdnProvider,
                fileUploder,
                false,
                0
        )

        then: 'IllegalArgumentException should be thrown with the appropriate message'
        IllegalArgumentException ex = thrown()
        ex.message == errorMessage

        where: 'The test data is as follows'
        cdnProvider        | fileUploder
        null               | null
        CDNProvider.GOOGLE | null
        CDNProvider.GOOGLE | Mock(GoogleCDNFileUploaderImpl)

        errorMessage << [
                'CDNProvider can not be null',
                'CDNFileUploader can not be null',
                'maxResultsInOneIteration can\'t be negative or zero'
        ]
    }

    void "test constructor when all are valid inputs"() {
        when: 'DefaultTemporaryUrlRenewer instance is created'
        DefaultTemporaryUrlRenewer renewer = defaultTemporaryUrlRenewerInstance

        then: 'No exception should be thrown and instance should be created successfully'
        noExceptionThrown()
        renewer
    }

    @Unroll
    void "test renew method when #description"() {
        setup: 'with cdnProvider instance'
        CDNFileUploader fileUploader = Mock(GoogleCDNFileUploaderImpl)

        fileUploader.getTemporaryURL(_, _, _) >> { String containerName, String fileName, long expiration ->
            if (cdnProviderThrowsException) {
                throw new StorageException('Failed')
            } else {
                return new Date().time.toString()
            }
        }

        and: 'with some ufile instances to process'
        (1..6).each {
            createUFileInstance(new Date() - 100)
        }

        (6..12).each {
            createUFileInstance(new Date() + 100)
        }

        and: 'Configuration for container name'
        Holders.flatConfig.put('fileuploader.groups.SomeFileGroup.container', 'dummy-container')

        when: 'The renew method is called'
        DefaultTemporaryUrlRenewer renewer = getDefaultTemporaryUrlRenewerInstance(forceAll)
        renewer.renew()

        then: 'No exception should be thrown'
        noExceptionThrown()

        where: 'The test data is as follows'
        forceAll | cdnProviderThrowsException
        false    | true
        false    | false
        true     | true
        true     | false

        description << [
                'forceAll is false and getTemporaryURL throws an Exception',
                'forceAll is false and getTemporaryURL does not throw an Exception',
                'forceAll is true and getTemporaryURL throws an Exception',
                'forceAll is true and getTemporaryURL does not throw an Exception'
        ]
    }

    void "test that getResultListFromOffset method returns the correct instances for the given offset"() {
        given: 'UFile instances'
        List<UFile> uFiles = []
        12.times { uFiles.add(createUFileInstance(new Date() - 100)) }

        and: 'Mocked CDNFileUploader instance'
        CDNFileUploader fileUploader = Mock(GoogleCDNFileUploaderImpl)

        when: 'getResultListFromOffset method is called'
        List<UFile> resultUFiles = getDefaultTemporaryUrlRenewerInstance(true, 5, fileUploader)
                .getResultListFromOffset(5)

        then: 'The returned resultUFiles must match with the expected list of UFiles'
        resultUFiles == uFiles[5..9]
    }

    void "test that processResultList method updates the expiration date and url for the given list of UFiles"() {
        given: 'UFile instances'
        List<UFile> uFiles = []
        5.times { uFiles.add(createUFileInstance(new Date() - 10)) }

        and: 'Mocked CDNFileUploader instance'
        CDNFileUploader fileUploader = Mock(GoogleCDNFileUploaderImpl)
        fileUploader.getTemporaryURL(_, _, _) >> { String containerName, String fileName, long expiration ->
            return 'Dummy Url'
        }

        and: 'Configuration for container name'
        Holders.flatConfig.put('fileuploader.groups.SomeFileGroup.container', 'dummy-container')

        when: 'processResultList method is called'
        getDefaultTemporaryUrlRenewerInstance(true, 5, fileUploader).processResultList(uFiles)

        then: 'path and expiresOn properties for the given files should be updated'
        uFiles.every { UFile uFile ->
            return uFile.path == 'Dummy Url' && uFile.expiresOn > new Date()
        }

        when: 'updateExpirationPeriodAndUrl method throws an exception'
        CDNFileUploader cdnFileUploader = Mock(GoogleCDNFileUploaderImpl)
        cdnFileUploader.getTemporaryURL(_, _, _) >> { String containerName, String fileName, long expiration ->
            throw new StorageException('Error')
        }

        DefaultTemporaryUrlRenewer defaultTemporaryUrlRenewer =
                getDefaultTemporaryUrlRenewerInstance(true, 5, cdnFileUploader)
        defaultTemporaryUrlRenewer.processResultList(uFiles)

        then: 'The thrown exception should be caught and exception should not get propagated'
        noExceptionThrown()
    }

    void "test that updateExpirationPeriodAndUrl method updates the expiration date and url for the given UFile"() {
        given: 'An UFile instance'
        UFile uFile = createUFileInstance(new Date())

        and: 'Mocked CDNFileUploader instance'
        CDNFileUploader fileUploader = Mock(GoogleCDNFileUploaderImpl)
        fileUploader.getTemporaryURL(_, _, _) >> { String containerName, String fileName, long expiration ->
            return 'Dummy Url'
        }

        and: 'Configuration for container name'
        Holders.flatConfig.put('fileuploader.groups.SomeFileGroup.container', 'dummy-container')

        when: 'updateExpirationPeriodAndUrl method is called'
        getDefaultTemporaryUrlRenewerInstance(true, 5, fileUploader)
                .updateExpirationPeriodAndUrl(uFile)

        then: 'path and expiresOn properties for the given file should be updated'
        uFile.path == 'Dummy Url' && uFile.expiresOn > new Date()

        when: 'The passed UFile instance cannot be updated'
        GroovyMock(NucleusUtils, global: true)
        NucleusUtils.save(_, _) >> false
        boolean result = getDefaultTemporaryUrlRenewerInstance(true, 5, fileUploader)
                .updateExpirationPeriodAndUrl(uFile)

        then: 'The method should return false'
        !result
    }

    @Unroll
    void "test that plugin respects the fileuploader.persistence.provider for value #persistenceProvider"() {
        given: 'Mocked CDNFileUploader instance'
        CDNFileUploader fileUploader = Mock(GoogleCDNFileUploaderImpl)

        and: 'fileuploader.persistence.provider configuration'
        Holders.config.put('fileuploader.persistence.provider', persistenceProvider)

        when: 'updateExpirationPeriodAndUrl method is called'
        Closure criteriaClosure = getDefaultTemporaryUrlRenewerInstance(true, 5, fileUploader).criteriaClosure()

        then: 'Criteria closure should contain mongodb specific criteria query'
        new JsonBuilder(criteriaClosure).toString().contains(expectedCriteriaString)

        where: 'The test data is as follows'
        persistenceProvider | expectedCriteriaString
        null                | '{"eq":["provider","GOOGLE"],"isNotNull":"expiresOn"}'
        'mongodb'           | '{"eq":["expiresOn",{"$exists":true}]}'
    }
}
