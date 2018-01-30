/*
 * Copyright (c) 2016, CauseCode Technologies Pvt Ltd, India.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or
 * without modification, are not permitted.
 */
package com.causecode.fileuploader

import com.causecode.fileuploader.embedded.EmUFile
import grails.util.Environment
import grails.util.Holders
import groovy.transform.EqualsAndHashCode

/**
 * A domain class which will hold the UFile related data.
 */
@EqualsAndHashCode
@SuppressWarnings(['GrailsDomainReservedSqlKeywordName', 'JavaIoPackageAccess'])
class UFile implements Serializable {

    private static final long serialVersionUID = 1

    int downloads

    CDNProvider provider

    Date dateUploaded = new Date()
    Date expiresOn

    Long size

    String extension
    String fileGroup
    String name
    String path
    /**
     * Contains calculated hash value of fileInputBean content
     */
    String checksum
    /**
     * Algorithm Used to calculate Checksum
     */
    String checksumAlgorithm

    UFileType type

    static transients = ['serialVersionUID']

    static constraints = {
        expiresOn nullable: true
        size min: 0L
        path blank: false
        name blank: false
        fileGroup blank: false
        provider nullable: true
        checksum nullable: true
        checksumAlgorithm nullable: true
    }

    static mapping = {
        path sqlType: 'text'
        /**
         * Checksum will be used to query UFile.
         */
        checksum index: true
        checksumAlgorithm index: true
    }

    def afterDelete() {
        /*
         * Using Holder class to get service instead of injecting it as dependency injection with transient modifier.
         * This prevents problem when we deserialize any instance of this class and the injected beans gets null value.
         */
        Holders.applicationContext['fileUploaderService'].deleteFileForUFile(this)
    }

    String searchLink() {
        Holders.applicationContext['fileUploaderService'].resolvePath(this)
    }

    boolean canMoveToCDN() {
        type == UFileType.LOCAL
    }

    boolean isFileExists() {
        new File(path).exists()
    }

    String getContainer() {
        containerName(Holders.flatConfig["fileuploader.groups.${fileGroup}.container"])
    }

    String getFullName() {
        name + '.' + extension
    }

    @Override
    String toString() {
        "UFile [$id][$fileGroup][$type]"
    }

    /**
     * A small helper method which returns the passed container name where the current environment name will be
     * appended if the current environment is not the Production environment. This is used to keep the containers
     * separate for all environment.
     *
     * @param containerName Name of the Amazon fileInputBean container or Google bucket.
     * @return Modified container name as described above.
     */
    static String containerName(String containerName) {
        if (!containerName) {
            return
        }

        if (Environment.current != Environment.PRODUCTION) {
            return containerName + '-' + Environment.current.name
        }

        return containerName
    }

    /**
     * Method to get Embedded Instance of UFile
     */
    EmUFile getEmbeddedInstance() {
        return new EmUFile([instanceId: this.id, downloads: this.downloads, expiresOn: this.expiresOn,
                            extension : this.extension, name: this.name, path: this.path])
    }
}

@SuppressWarnings(['GrailsDomainHasEquals'])
enum UFileType {

    CDN_PRIVATE(1),
    CDN_PUBLIC(2),
    LOCAL(3)

    final int id

    UFileType(int id) {
        this.id = id
    }

    @Override
    String toString() {
        "${this.name()}($id)"
    }
}

@SuppressWarnings(['GrailsDomainHasEquals'])
enum CDNProvider {

    AMAZON(1),
    RACKSPACE(2),
    GOOGLE(3),
    LOCAL(4)

    final int id

    CDNProvider(int id) {
        this.id = id
    }

    @Override
    String toString() {
        "${this.name()}($id)"
    }
}
