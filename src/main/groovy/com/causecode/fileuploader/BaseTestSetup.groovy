/*
 * Copyright (c) 2016, CauseCode Technologies Pvt Ltd, India.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or
 * without modification, are not permitted.
 */
package com.causecode.fileuploader

/**
 * This class contains common setup that can be used in unit, functional and integration test cases.
 *
 * @author Ankit Agrawal
 * @Since 3.0.1
 */
@SuppressWarnings(['JavaIoPackageAccess'])
trait BaseTestSetup {

    // Remember to add cleanup block to delete new file after calling this method.
    File getFileInstance(String filePath) {
        File file = new File(filePath)
        file.createNewFile()
        file << 'This is a test document.'
    }
}
