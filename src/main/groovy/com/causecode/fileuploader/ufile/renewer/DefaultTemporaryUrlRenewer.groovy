/*
 * Copyright (c) 2011-Present, CauseCode Technologies Pvt Ltd, India.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or
 * without modification, are not permitted.
 */
package com.causecode.fileuploader.ufile.renewer

import static com.google.common.base.Preconditions.checkArgument

import com.causecode.fileuploader.CDNProvider
import com.causecode.fileuploader.StorageException
import com.causecode.fileuploader.UFile
import com.causecode.fileuploader.UFileType
import com.causecode.fileuploader.cdn.CDNFileUploader
import com.causecode.fileuploader.util.Time
import com.causecode.util.NucleusUtils
import grails.util.Holders
import groovy.util.logging.Slf4j

/**
 * This renewer, fetches UFiles of given CDNProvider and then renews its temporary urls.
 *
 * @author Milan Savaliya
 * @author Hardik Modha
 * @since 3.1.3
 */
@Slf4j
@SuppressWarnings('FieldName') // Regex for final fields is incorrect in CodeNarc rules
class DefaultTemporaryUrlRenewer implements TemporaryUrlRenewer {

    private final CDNProvider cdnProvider
    private final CDNFileUploader cdnFileUploader
    private final int maxResultsInOneIteration
    private final boolean forceAll

    DefaultTemporaryUrlRenewer(
            CDNProvider cdnProvider,
            CDNFileUploader cdnFileUploader,
            boolean forceAll,
            int maxResultsInOneIteration = 100) {

        checkArgument(cdnProvider != null, 'CDNProvider can not be null')
        checkArgument(cdnFileUploader != null, 'CDNFileUploader can not be null')
        checkArgument(maxResultsInOneIteration > 0, 'maxResultsInOneIteration can\'t be negative or zero')

        this.cdnProvider = cdnProvider
        this.cdnFileUploader = cdnFileUploader
        this.forceAll = forceAll
        this.maxResultsInOneIteration = Math.min(maxResultsInOneIteration, 1000)
    }

    @Override
    void renew() {
        int offset = 0
        List<UFile> uFiles

        while ((uFiles = getResultListFromOffset(offset)).size()) {
            processResultList(uFiles)
            offset += this.maxResultsInOneIteration
        }

        cdnFileUploader.close()
    }

    private List<UFile> getResultListFromOffset(int offset) {
        return UFile.createCriteria().list([offset: offset, max: this.maxResultsInOneIteration], criteriaClosure())
    }

    private Closure criteriaClosure() {
        return {
            eq('type', UFileType.CDN_PUBLIC)
            eq('provider', this.cdnProvider)

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
        }
    }

    private List<UFile> processResultList(List<UFile> resultList) {
        resultList.each { UFile uFile ->
            log.debug "Renewing URL for $uFile"

            try {
                updateExpirationPeriodAndUrl(uFile)
            } catch (StorageException ex) {
                log.error("URL is not generated for File: $uFile due to $ex.message", ex)
            }
        }
    }

    @SuppressWarnings('CannotModifyReference') // Method just saves the given instance after updating the properties
    private boolean updateExpirationPeriodAndUrl(UFile uFile) {
        long expirationPeriod = getExpirationPeriod(uFile.fileGroup)

        uFile.path = cdnFileUploader.getTemporaryURL(uFile.container, uFile.fullName, expirationPeriod)
        uFile.expiresOn = new Date(new Date().time + expirationPeriod * 1000)

        if (NucleusUtils.save(uFile, true)) {
            log.debug "New URL for $uFile [$uFile.path] [$uFile.expiresOn]"

            return true
        }

        return false
    }

    private long getExpirationPeriod(String fileGroup) {
        // Default to 30 Days
        return Holders.flatConfig["fileuploader.groups.${fileGroup}.expirationPeriod"] ?: (Time.DAY * 30)
    }
}
