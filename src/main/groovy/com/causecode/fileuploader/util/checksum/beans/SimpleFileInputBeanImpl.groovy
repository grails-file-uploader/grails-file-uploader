/*
 * Copyright (c) 2011 - Present, CauseCode Technologies Pvt Ltd, India.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or
 * without modification, are not permitted.
 */
package com.causecode.fileuploader.util.checksum.beans

/**
 * Bean to handle java.io.File inputs to calculate hash/checksum
 * @author Milan Savaliya
 * @since 3.1.0
 */
class SimpleFileInputBeanImpl implements FileInputBean {

    private final File FILE

    SimpleFileInputBeanImpl(File file) {
        this.FILE = file
        validateInputs()
    }

    /**
     * Method validates supplied inputs and throws appropriate exception if inputs are not in proper format.
     * @throws IllegalArgumentException , FileNotFoundException
     * */
    private void validateInputs() throws IllegalArgumentException, FileNotFoundException {
        if (!this.FILE) {
            throw new IllegalArgumentException('File instance can not be null')
        }

        if (!this.FILE.exists()) {
            throw new FileNotFoundException("File with name ${this.FILE.name} not found")
        }
    }

    @Override
    String getName() {
        return this.FILE.name
    }

    @Override
    String getOriginalFilename() {
        return this.FILE.name
    }

    @Override
    String getContentType() throws IOException {
        String fileName = this.FILE.name
        if (fileName.lastIndexOf('.') != -1) {
            return fileName[fileName.lastIndexOf('.') + 1..fileName.length() - 1]
        }

        return null
    }

    @Override
    boolean isEmpty() {
        return this.FILE.size() == 0
    }

    @Override
    long getSize() {
        return this.FILE.size()
    }

    @Override
    byte[] getBytes() throws IOException {
        return this.FILE.readBytes()
    }

    /**
     * User needs to close returned input stream.
     * @return InputStream
     * @throws IOException
     */
    @Override
    InputStream getInputStream() throws IOException {
        return new FileInputStream(this.FILE)
    }

    @Override
    boolean isExists() {
        return this.FILE.exists()
    }
}
