/*
 * Copyright (c) 2017-Present, CauseCode Technologies Pvt Ltd, India.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or
 * without modification, are not permitted.
 */
package com.causecode.fileuploader.util.process

import com.causecode.fileuploader.CDNProvider
import com.causecode.fileuploader.StorageException
import com.causecode.fileuploader.UFile
import com.causecode.fileuploader.UFileType
import com.causecode.fileuploader.cdn.CDNFileUploader
import com.causecode.fileuploader.cdn.google.GoogleCDNFileUploaderImpl
import com.causecode.fileuploader.util.UFileTemporaryUrlRenewer
import grails.buildtestdata.mixin.Build
import grails.util.Holders
import spock.lang.Specification
import spock.lang.Unroll

/**
 * Unit tests for {@link UFileTemporaryUrlRenewer}
 *
 * @author Milan Savaliya
 * @since 3.1.1
 */
@Build([UFile])
class UFileTemporaryUrlRenewerSpec extends Specification {

    @Unroll
    void "test constrcutor when inputs are not valid"() {
        when:
        UFileTemporaryUrlRenewer.newInstance(
                cdnProvider,
                fileUploder,
                false,
                maxResult
        )

        then:
        IllegalArgumentException ex = thrown()
        ex.message == errorMessage

        where:
        cdnProvider        | fileUploder                     | maxResult
        null               | null                            | 0
        CDNProvider.GOOGLE | null                            | 0
        CDNProvider.GOOGLE | Mock(GoogleCDNFileUploaderImpl) | 0


        errorMessage << [
                'CDNProvider can not be null',
                'CDNFileUploader can not be null',
                'maxResultsInOneIteration can\'t be negative or zero'
        ]
    }

    void "test constructor when all are valid inputs"() {
        when:
        UFileTemporaryUrlRenewer renewer = UFileTemporaryUrlRenewer.newInstance(
                CDNProvider.GOOGLE,
                Mock(GoogleCDNFileUploaderImpl),
                false,
                10
        )

        then:
        noExceptionThrown()
        renewer
    }

    @Unroll
    void "test start method when #description"() {
        setup: 'with cdnProvider instance'
        CDNFileUploader fileUploader = Mock(GoogleCDNFileUploaderImpl)
        if (cdnProviderThrowsException) {
            fileUploader.getTemporaryURL(_, _, _) >> { String containerName, String fileName, long expiration ->
                throw new StorageException('Failed')
            }
        } else {
            fileUploader.getTemporaryURL(_, _, _) >> { String containerName, String fileName, long expiration ->
                return new Date().getTime().toString()
            }
        }

        and: 'with some ufile instances to process'
        (1..10).each { int index ->
            UFile.build(
                    downloads: index,
                    provider: CDNProvider.GOOGLE,
                    size: 100,
                    path: new Date().getTime().toString(),
                    name: new Date().getTime().toString(),
                    fileGroup: 'SomeFileGroup',
                    type: UFileType.CDN_PUBLIC,
                    expiresOn: new Date()
            )
        }

        and:
        Holders.flatConfig.put("fileuploader.groups.SomeFileGroup.container", 'tendering-staging')

        when:
        UFileTemporaryUrlRenewer renewer = new UFileTemporaryUrlRenewer(
                CDNProvider.GOOGLE,
                fileUploader,
                forceAll, 5)
        renewer.start()

        then:
        noExceptionThrown()

        where:
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
}
