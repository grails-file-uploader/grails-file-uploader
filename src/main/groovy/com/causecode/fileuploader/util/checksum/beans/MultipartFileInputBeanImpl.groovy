package com.causecode.fileuploader.util.checksum.beans

import org.springframework.web.multipart.MultipartFile

/**
 * Bean to handle Multipart File Uploads to calculate Hash/Checksum
 * @author Milan Savaliya
 */
class MultipartFileInputBeanImpl implements FileInputBean {

    private final MultipartFile multipartFile

    MultipartFileInputBeanImpl(MultipartFile multipartFile) {
        this.multipartFile = multipartFile
        validateInputs()
    }

    private void validateInputs() {
        if (this.multipartFile == null) {
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
