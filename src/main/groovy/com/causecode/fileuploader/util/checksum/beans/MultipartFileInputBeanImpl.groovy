package com.causecode.fileuploader.util.checksum.beans

import org.springframework.web.multipart.MultipartFile

/**
 * Bean to handle Multipart File Uploads to calculate Hash/Checksum
 * @author Milan Savaliya
 */
class MultipartFileInputBeanImpl implements FileInputBean {

    private MultipartFile multipartFile

    MultipartFileInputBeanImpl(MultipartFile multipartFile) {
        this.multipartFile = multipartFile
        validateInputs()
    }

    private void validateInputs() {
        if (this.multipartFile == null) {
            throw new IllegalArgumentException("Multipart Instance can not be null")
        }
    }

    @Override
    String getName() {
        return this.multipartFile.getName()
    }

    @Override
    String getOriginalFilename() {
        return this.multipartFile.getOriginalFilename()
    }

    @Override
    String getContentType() {
        return this.multipartFile.getContentType()
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
        return this.multipartFile.getBytes()
    }

    @Override
    InputStream getInputStream() throws IOException {
        return this.multipartFile.getInputStream()
    }

    @Override
    boolean isExists() {
        return true
    }
}
