/*
 * Copyright (c) 2018, CauseCode Technologies Pvt Ltd, India.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or
 * without modification, are not permitted.
 */

package com.causecode.fileuploader.util.checksum.beans

import org.springframework.web.multipart.MultipartFile

/**
 * Bean to handle Multipart File Uploads to calculate Hash/Checksum
 * @author Milan Savaliya
 * @since 3.1.0
 */
class MultipartFileInputBeanImpl implements FileInputBean {

    //Instance of MultipartFile Bean
    private final MultipartFile multipartFile

    /**
     * Constructor to instantiate MultipartFileInputBeanImpl instance
     * @param multipartFile
     */
    MultipartFileInputBeanImpl(MultipartFile multipartFile) {
        this.multipartFile = multipartFile
        validateInputs()
    }

    /**
     * Method checks supplied inputs and throws appropriate exception if input is not in format as expected.
     */
    private void validateInputs() throws IllegalArgumentException{
        if (!this.multipartFile) {
            throw new IllegalArgumentException('Multipart Instance can not be null')
        }

    }

    @Override
    String getName() {
        return this.multipartFile.name
    }

    @Override
    String getOriginalFilename() {
        return this.multipartFile.originalFilename
    }

    @Override
    String getContentType() {
        return this.multipartFile.contentType
    }

    @Override
    boolean isEmpty() {
        return this.multipartFile.isEmpty()
    }

    @Override
    long getSize() {
        return this.multipartFile.size
    }

    @Override
    byte[] getBytes() throws IOException {
        return this.multipartFile.bytes
    }

    @Override
    InputStream getInputStream() throws IOException {
        return this.multipartFile.inputStream
    }

    @Override
    boolean isExists() {
        return true
    }
}
