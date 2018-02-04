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
    private final File file

    SimpleFileInputBeanImpl(File file) {
        this.file = file
        validateInputs()
    }

    /**
     * Method validates supplied inputs and throws appropriate exception if inputs are not in proper format.
     * @throws IllegalArgumentException , FileNotFoundException
     * */
    private void validateInputs() throws IllegalArgumentException, FileNotFoundException {
        if (!this.file) {
            throw new IllegalArgumentException('File instance can not be null')
        }

        if (!this.file.exists()) {
            throw new FileNotFoundException("File with name ${this.file.name} not found")
        }

    }

    @Override
    String getName() {
        return this.file.name
    }

    @Override
    String getOriginalFilename() {
        return this.file.name
    }

    @Override
    String getContentType() {
        return ''
    }

    @Override
    boolean isEmpty() {
        return this.file.size() == 0
    }

    @Override
    long getSize() {
        return this.file.size()
    }

    @Override
    byte[] getBytes() throws IOException {
        return this.file.readBytes()
    }

    /**
     * User needs to close returned input stream.
     * @return InputStream
     * @throws IOException
     */
    @Override
    InputStream getInputStream() throws IOException {
        return new FileInputStream(this.file)
    }

    @Override
    boolean isExists() {
        return this.file.exists()
    }
}
