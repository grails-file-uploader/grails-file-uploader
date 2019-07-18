package com.causecode.fileuploader.ufile

import com.causecode.fileuploader.CDNProvider
import com.causecode.fileuploader.ProviderNotFoundException
import com.causecode.fileuploader.cdn.CDNFileUploader
import com.causecode.fileuploader.provider.ProviderService
import com.causecode.fileuploader.ufile.renewer.DefaultTemporaryUrlRenewer
import com.causecode.fileuploader.ufile.renewer.TemporaryUrlRenewer

/**
 * Service which communicates with UFileTemporaryUrlRenewers.
 *
 * @author Milan Savaliya
 * @since 3.1.3
 */
class TemporaryUrlRenewerService {

    ProviderService providerService

    void renewTemporaryURL(boolean forceAll = false) throws ProviderNotFoundException, IllegalArgumentException {
        CDNProvider[] allProviders = CDNProvider.values()
        CDNProvider[] providersToExclude = [CDNProvider.RACKSPACE, CDNProvider.LOCAL]

        (allProviders - providersToExclude).each { CDNProvider cdnProvider ->
            CDNFileUploader cdnFileUploaderInstance = providerService.getProviderInstance(cdnProvider.name())

            TemporaryUrlRenewer temporaryUrlRenewer =
                    new DefaultTemporaryUrlRenewer(cdnProvider, cdnFileUploaderInstance, forceAll)

            log.debug "Renewing TemporaryUrls for the $cdnProvider"

            temporaryUrlRenewer.renew()
        }
    }
}
