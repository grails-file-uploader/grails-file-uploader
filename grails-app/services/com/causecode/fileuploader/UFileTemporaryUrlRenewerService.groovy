/*
 * Copyright (c) 2011-Present, CauseCode Technologies Pvt Ltd, India.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or
 * without modification, are not permitted.
 */
package com.causecode.fileuploader

import com.causecode.fileuploader.cdn.CDNFileUploader
import com.causecode.fileuploader.util.renewer.DefaultUFileTemporaryUrlRenewer
import com.causecode.fileuploader.util.renewer.UFileTemporaryUrlRenewer
import grails.transaction.Transactional

/**
 * Service which communicates with UFileTemporaryUrlRenewers.
 *
 * @author Milan Savaliya
 * @since 3.
 */
@Transactional
class UFileTemporaryUrlRenewerService {

    UtilitiesService utilitiesService

    void renewTemporaryURL(boolean forceAll = false) {
        CDNProvider[] allProviders = CDNProvider.values()
        CDNProvider[] providersToExclude = [CDNProvider.RACKSPACE, CDNProvider.LOCAL]

        (allProviders - providersToExclude).each { CDNProvider cdnProvider ->
            CDNFileUploader cDNFileUploader = utilitiesService.getProviderInstance(cdnProvider.name())

            UFileTemporaryUrlRenewer temporaryUrlRenewer =
                    new DefaultUFileTemporaryUrlRenewer(cdnProvider, cDNFileUploader, forceAll)

            log.debug "Renewing TemporaryUrls for the $cdnProvider"

            temporaryUrlRenewer.renew()
        }
    }
}
