/*
 * Copyright (c) 2017, CauseCode Technologies Pvt Ltd, India.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or
 * without modification, are not permitted.
 */

import com.causecode.fileuploader.CDNProvider
import com.causecode.fileuploader.UFileType

/**
 * This file contains Test data configurations which is going to be used by build-test-data plugin.
 */

// Defining default values to be used while creating a UFile instance.
testDataConfig {
    sampleData {
        'com.causecode.fileuploader.UFile' {
            provider = CDNProvider.GOOGLE
            path = './temp/test.txt'
            fileGroup = 'testGoogle'
        }
    }
}
