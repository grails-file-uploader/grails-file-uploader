/*
 * Copyright (c) 2011-Present, CauseCode Technologies Pvt Ltd, India.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or
 * without modification, are not permitted.
 */
package com.causecode.fileuploader.util

import com.causecode.fileuploader.CDNProvider
import com.causecode.fileuploader.FileUploaderService
import com.causecode.fileuploader.UFile
import com.causecode.fileuploader.UFileType
import com.causecode.fileuploader.UtilitiesService
import com.causecode.fileuploader.cdn.CDNFileUploader
import com.causecode.util.NucleusUtils
import com.google.common.base.Preconditions
import grails.util.Holders
import groovy.util.logging.Slf4j

@Slf4j
class UFileTemporaryUrlRenewer {
    UtilitiesService utilitiesService

    UFileTemporaryUrlRenewer(
            CDNProvider cdnProvider,
            CDNFileUploader cdnFileUploader,
            int maxUFileToProcessInOneIteration = 100) {
        Preconditions.checkArgument(cdnProvider != null, 'CDNProvider can not be null')
        Preconditions.checkArgument(cdnFileUploader != null, 'CDNFileUploader can not be null')
        this.cdnProvider = cdnProvider
        this.cdnFileUploader = cdnFileUploader
        this.maxUFileToProcessInOneIteration = maxUFileToProcessInOneIteration
    }

    void mainLogic() {
        UFile.withCriteria {
            eq('type', UFileType.CDN_PUBLIC)
            eq('provider', cdnProvider)

            if (Holders.flatConfig['fileuploader.persistence.provider'] == 'mongodb') {
                eq('expiresOn', [$exists: true])
            } else {
                isNotNull('expiresOn')
            }

            if (!forceAll) {
                or {
                    lt('expiresOn', new Date())
                    // Getting all CDN UFiles which are about to expire within one day.
                    between('expiresOn', new Date(), new Date() + 1)
                }
            }

            maxResults(this.maxUFileToProcessInOneIteration)
        }.each { UFile ufileInstance ->
            log.debug "Renewing URL for $ufileInstance"

            long expirationPeriod = getExpirationPeriod(ufileInstance.fileGroup)

            ufileInstance.path = fileUploaderInstance.getTemporaryURL(ufileInstance.container,
                    ufileInstance.fullName, expirationPeriod)
            ufileInstance.expiresOn = new Date(new Date().time + expirationPeriod * 1000)
            NucleusUtils.save(ufileInstance, true)

            log.debug "New URL for $ufileInstance [$ufileInstance.path] [$ufileInstance.expiresOn]"
        }

        fileUploaderInstance.close()
    }

    long getExpirationPeriod(String fileGroup) {
        // Default to 30 Days
        return Holders.flatConfig["fileuploader.groups.${fileGroup}.expirationPeriod"] ?: (Time.DAY * 30)
    }


}
