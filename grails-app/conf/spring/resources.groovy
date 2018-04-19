/*
 * Copyright (c) 2017-Present, Niteo Consulting Private Limited, India.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or
 * without modification, are not permitted.
 */
package spring

import com.causecode.fileuploader.util.UFileTemporaryUrlRenewer

beans = {
    uFileTemporaryUrlRenewer(UFileTemporaryUrlRenewer) { bean ->
        bean.autowire = 'byName'
    }
}
