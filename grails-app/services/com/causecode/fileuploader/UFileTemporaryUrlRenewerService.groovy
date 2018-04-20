/*
 * Copyright (c) 2011-Present, CauseCode Technologies Pvt Ltd, India.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or
 * without modification, are not permitted.
 */
package com.causecode.fileuploader

import com.causecode.fileuploader.cdn.CDNFileUploader
import com.causecode.fileuploader.util.UFileTemporaryUrlRenewer
import grails.transaction.Transactional
import groovy.util.logging.Slf4j

/**
 * Service which communicates with UFileTemporaryUrlRenewers.
 *
 * @author Milan Savaliya
 * @since 3.1.1
 */
@Transactional
@Slf4j
class UFileTemporaryUrlRenewerService {

    UtilitiesService utilitiesService

    void renewTemporaryURL(boolean forceAll = false) {
        CDNProvider[] allProviders = CDNProvider.values()
        CDNProvider[] providersToExclude = [CDNProvider.RACKSPACE, CDNProvider.LOCAL]

        (allProviders - providersToExclude).each { CDNProvider cdnProvider ->
            CDNFileUploader cDNFileUploader = utilitiesService.getProviderInstance(cdnProvider.name())
            UFileTemporaryUrlRenewer renewer = new UFileTemporaryUrlRenewer(cdnProvider, cDNFileUploader, forceAll)
            renewer.start()
        }
    }
}
