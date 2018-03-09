/*
 * Copyright (c) 2011 - Present, CauseCode Technologies Pvt Ltd, India.
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
    private final MultipartFile MUTLTIPART_FILE

    /**
     * Constructor to instantiate MultipartFileInputBeanImpl instance
     * @param multipartFile
     */
    MultipartFileInputBeanImpl(MultipartFile multipartFile) {
        this.MUTLTIPART_FILE = multipartFile
        validateInputs()
    }

    /**
     * Method checks supplied inputs and throws appropriate exception if input is not in format as expected.
     */
    private void validateInputs() throws IllegalArgumentException{
        if (!this.MUTLTIPART_FILE) {
            throw new IllegalArgumentException('Multipart Instance can not be null')
        }
    }

    @Override
    String getName() {
        return this.MUTLTIPART_FILE.name
    }

    @Override
    String getOriginalFilename() {
        return this.MUTLTIPART_FILE.originalFilename
    }

    @Override
    String getContentType() {
        return this.MUTLTIPART_FILE.contentType
    }

    @Override
    boolean isEmpty() {
        return this.MUTLTIPART_FILE.isEmpty()
    }

    @Override
    long getSize() {
        return this.MUTLTIPART_FILE.size
    }

    @Override
    byte[] getBytes() throws IOException {
        return this.MUTLTIPART_FILE.bytes
    }

    @Override
    InputStream getInputStream() throws IOException {
        return this.MUTLTIPART_FILE.inputStream
    }

    /**
     * Whenever There is valid MultipartFile object available, file will surely exists on server. So this method returns
     * @return
     */
    @Override
    boolean isExists() {
        return true
    }
}
