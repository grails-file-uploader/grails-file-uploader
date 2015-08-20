package com.lucastex.grails.fileuploader

import grails.util.Environment
import grails.util.Holders

class UFile implements Serializable {

    int downloads

    CDNProvider provider

    Date dateUploaded = new Date()
    Date expiresOn

    Long size

    String extension
    String fileGroup
    String name
    String path

    UFileType type

    static constraints = {
        expiresOn nullable: true
        size min: 0L
        path blank: false
        name blank: false
        fileGroup blank: false
        provider nullable: true
    }

    def afterDelete() {
        /*
         * Using Holder class to get service instead of injecting it as dependency injection with transient modifier.
         * This prevents problem when we deserialize any instance of this class and the injected beans gets null value.
         */
        Holders.getApplicationContext()["fileUploaderService"].deleteFileForUFile(this)
    }

    String searchLink() {
        Holders.getApplicationContext()["fileUploaderService"].resolvePath(this)
    }

    boolean canMoveToCDN() {
        type == UFileType.LOCAL
    }

    boolean isFileExists() {
        new File(path).exists()
    }

    String getContainer() {
        containerName(Holders.getFlatConfig()["fileuploader.${fileGroup}.container"])
    }

    String getFullName() {
        name + "." + extension
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
     * @param containerName Name of the Amazon file container or Rackspace bucket.
     * @return Modified container name as described above.
     */
    static String containerName(String containerName) {
        if (Environment.current != Environment.PRODUCTION) {
            return containerName + "-" + Environment.current.name
        }

        return containerName
    }
}

enum UFileType {

    CDN_PRIVATE(1),
    CDN_PUBLIC(2),
    LOCAL(3)

    final int id
    UFileType(int id) {
        this.id = id
    }
}

enum CDNProvider {

    AMAZON(1),
    RACKSPACE(2)

    final int id
    CDNProvider(int id) {
        this.id = id
    }
}