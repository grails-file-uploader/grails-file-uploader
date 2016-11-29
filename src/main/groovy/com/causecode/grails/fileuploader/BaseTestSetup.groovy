/*
 * Copyright (c) 2016, CauseCode Technologies Pvt Ltd, India.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or
 * without modification, are not permitted.
 */
package com.causecode.grails.fileuploader

/**
 * This class contains common setup that can be used in unit, functional and integration test cases.
 * It helps in getting data maps or instances for any domain so that we don't have to repeat the creation of data
 * maps or instances for each and every test case.
 *
 * @author Ankit Agrawal
 * @Since 3.0.1
 */
@SuppressWarnings(['JavaIoPackageAccess'])
trait BaseTestSetup {

    static final Map SAVE_FLUSH = [flush: true]

    // UFile
    Map getUFileDataMap(int index) {
        Map dataMap = [downloads: index, provider: CDNProvider.GOOGLE, size: 1L, extension: 'jpg',
                fileGroup: 'testGoogle', name: "test-file-$index", path: './temp/test.txt', type: UFileType.LOCAL]

        return dataMap
    }

    UFile getUFileInstance(int index) {
        Map uFileDataMap = getUFileDataMap(index)
        UFile ufileInstance = new UFile(uFileDataMap)
        ufileInstance.save(SAVE_FLUSH)
        assert ufileInstance.id

        return ufileInstance
    }

    // UFileMoveHistory
    Map getUFileMoveHistoryDataMap(int index) {
        Map dataMap = [ufile: getUFileDataMap(index), moveCount: index, lastUpdated: new Date(),
                fromCDN: CDNProvider.RACKSPACE, toCDN: CDNProvider.GOOGLE, status: MoveStatus.FAILURE,
                details: 'Moved file from rackspace to google cloud']

        return dataMap
    }

    UFileMoveHistory getUFileMoveHistoryInstance(int index) {
        Map ufileMoveHistoryDataMap = getUFileMoveHistoryDataMap(index)
        UFileMoveHistory uFileMoveHistoryInstance = new UFileMoveHistory(ufileMoveHistoryDataMap)
        uFileMoveHistoryInstance.save(SAVE_FLUSH)
        assert uFileMoveHistoryInstance.id

        return uFileMoveHistoryInstance
    }

    // Remember to add cleanup block to delete new file after calling this method.
    File getFileInstance() {
        File file = new File('./temp/test.txt')
        file.createNewFile()
        file << 'This is a test document.'
    }
}
