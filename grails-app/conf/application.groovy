/*
 * Copyright (c) 2016, CauseCode Technologies Pvt Ltd, India.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or
 * without modification, are not permitted.
 */

import com.causecode.fileuploader.*
import com.causecode.fileuploader.util.*

grails{
    profile = 'web-plugin'
    codegen{
        defaultPackage = 'grails.file.uploader'
    }
}

info{
    app{
        name = '@info.app.name@'
        version = '@info.app.version@'
        grailsVersion = '@info.app.grailsVersion@'
    }
}

spring {
    groovy{
        template {
            checkTemplateLocation = false
        }
    }
}

fileuploader {
    groups {
        user {
            maxSize = 1024 * 1024 * 2 // 2 MB
            allowedExtensions = ["jpg", "jpeg", "gif", "png"]
            storageTypes = "CDN"
            container = "causecode-1"
            provider = CDNProvider.AMAZON
            makePublic = true
        }
        image {
            maxSize = 1024 * 1024 * 2 // 2 MB
            allowedExtensions = ["jpg", "jpeg", "gif", "png"]
            storageTypes = "CDN"
            container = "causecode-1"
            provider = CDNProvider.AMAZON
            expirationPeriod = 24 * 60 * 60 * 1 * 365L
        }
        profile {
            maxSize = 1024 * 1024 * 2 // 2 MB
            allowedExtensions = ["jpg", "jpeg", "gif", "png"]
            storageTypes = "CDN"
            container = "causecode-1"
            provider = CDNProvider.AMAZON
            expirationPeriod = 24 * 60 * 60 * 1 * 365L
        }
        testGoogle {
            maxSize = 1024 * 1024 * 2 // 2 MB
            allowedExtensions = ["jpg", "jpeg", "gif", "png", "txt"]
            storageTypes = "CDN"
            container = "causecode"
            provider = CDNProvider.GOOGLE
            expirationPeriod = 24 * 60 * 60 * 1 * 365L
        }
        testAmazon {
            maxSize = 1024 * 1024 * 2 // 2 MB
            allowedExtensions = ["jpg", "jpeg", "gif", "png", "txt"]
            storageTypes = "CDN"
            container = "causecode"
            provider = CDNProvider.AMAZON
            expirationPeriod = 24 * 60 * 60 * 1 * 365L
        }
        testLocal {
            maxSize = 1024 * 1024 * 2 // 2 MB
            allowedExtensions = ["jpg", "jpeg", "gif", "png", "txt"]
            path = './temp'
        }
    }

    storageProvider {
        amazon {
            AmazonKey = "RANDOM_KEY"
            AmazonSecret = "RANDOM_SECRET"
        }

        google {
            // The path of the JSON Key file.
            authFile = 'testkey.json'
            project_id = 'test_id'
            client_id = 'test_client_id'
            client_email = 'test@email.com'
            private_key = "-----BEGIN PRIVATE KEY-----\nMIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQC1plnUQ" +
                    "kO0H1qj\nrrrrrrrwoI2N9wmXopHSdrmtL6cfPo3uzEMCXB9/evmDn6y/KgZluqn1adGRABr+\nVI/hg5y4QWx1On/vg" +
                    "VrNehCkGExJcmhvbDc9XsKe7yrH6ix9kwq+UuDfbaVQP4dG\n+b9jzKhhBN77CafM6foYdW6NV9WBb/IfIYR5+ecjwLD" +
                    "KmOV7yuh4dxRYiXrxAUth\n3fU6Mnlsn24sdjhfjsQGdABMcM1MBcOEXuNDma/GvGBAwojLhV+HEvM7+JoU2rPF\nAUs" +
                    "MP1YEgbhhoIUnL8jZlCxc5egFuExQJ8NFmLA0r/di8SJdVRmQ9YwXZrNL/E56\nizd7IkPfAgMBAAECggEAKIoBji9js" +
                    "LU2oyHj7NqW5KfQL5isWVz8sj2w3oe+AmkR\nf/OyGLq6hNbLDKb8BIW6e8WW3KBLFtMxMwVoPuoCddSUAe2WU7tIqob" +
                    "NY/HQRKv1\nxrgd8+JMCPBTWd5XI4dHZIZWjUaJGEm1RQ/DuOAZy90sblYPTtoA6Kh8jarGiWY0\nDcmINJ1amAK9y5Z" +
                    "s3YxiI2nibEU5Ukg+WYYlkp1P87lXHkEZdbjaLZjMOlreulFP\nMkQJCSvP1K8BhG0egy6WocurXi1XxO2fgV2I4ttio" +
                    "progjsE0n6ckYlN8Rn/RRL7\n2M/xdT8jHwoFT9okkpEPKL0i4rcwWzjbVRmCGaKoUQKBgQDgI7nCMTKL+xtxrT3a\nb" +
                    "N7N9WkTcEhYwzR8z2e/Kb8WEPAhocRwSlElrGII1f13cH8VZYfaHYBRK3EfnWj3\n54tePmd8QIkvcQ52LzdTHu2ug69" +
                    "wXx/fSIeX6VAEUq8CenvnVRIdxBPgD+SQnzTb\n/v3VQr2rE/tC4PN2HsMOBHLfowKBgQDPeHRce9xExBWepAU8NReWM" +
                    "TW9BxDm9QcU\nw3YxJmtKshejKsAnrqQqDbRl9PqCnA8tevRiFA5cLrmoi9l/Pf4+d8hk/d+KzkVa\nsWP9uy92Udnkp" +
                    "HMDGYMi1BaoVJGOXBz6UhQZ/E9aK37oJkmNYjafhkadfgkksdK9\n/+k4xrselQKBgFWDy9+XPZ3ClFnuquanGuqhcxG" +
                    "DiVa0Q1ZLG14H42vb0oJCYdwl\nvogNGOoeGrJzLUlQj2BcWRSe6m4RdHcA1F+El58EUFLVY5sbsJP5/NzUSR8qa9KY\n" +
                    "rYL79RdxJGxhptw/zMWmhZ668Y1r8JLu2pF/ATZATWDayoiKfAjhEaLZAoGBAIlB\nWlH5C29e/iKWxnPfd/xAEBo/O3" +
                    "pgLlAZQ+8eSKsBCUxEMx3NlwPPT0KIgNa5ofzn\ntBfSxvFLZD5STxC0FaiEHEpb/nDZJ97pLoqlNLAlVG7EMOajETOQ" +
                    "dnDkietRm0Yr\n2OqcFm9ECVBSTCTCC2EDGhQP8Oxbu6uBS2zpZM/FAoGAEZSUVj8CwBh9iPQtjlAp\no0CS8wMxeLKw" +
                    "ge6ft9BCgGieEmYOq02mt6c52vez/rrrrrrQbsIo+mV4ZbQs0mgS\nYPrqYpFr+8eeZsQ0jIv+hUQW35HYoRLOkeuK+L" +
                    "SRVHUcHBpJ+bsPOrrUFiZYVMCI\n1231etEo75OxbK4sskK+gUsaaaaaaaaaaaaa=\n-----END PRIVATE KEY-----\n"
            private_key_id = 'test_123'
            type = 'service_account'
        }
    }
}

environments {
    test {
        grails.gorm.autoFlush = true
    }
}
