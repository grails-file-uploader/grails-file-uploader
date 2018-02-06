/*
 * Copyright (c) 2011 - Present, CauseCode Technologies Pvt Ltd, India.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or
 * without modification, are not permitted.
 */
package com.causecode.fileuploader.util.checksum.beans

/**
 * File InputBean to work with Hash Calculation
 * @author Milan Savaliya
 * @since 3.1.0
 */
interface FileInputBean {
    /**
     * Return the name of the parameter in the multipart form.
     * @return the name of the parameter (never {@code null} or empty)
     */
    String getName()

    /**
     * Return the original filename in the client's filesystem.
     * <p>This may contain path information depending on the browser used,
     * but it typically will not with any other than Opera.
     * @return the original filename, or the empty String if no fileInputBean
     * has been chosen in the multipart form, or {@code null}
     * if not defined or not available
     */
    String getOriginalFilename()

    /**
     * Return the content type of the fileInputBean.
     * @return the content type, or {@code null} if not defined
     * (or no fileInputBean has been chosen in the multipart form)
     */
    String getContentType() throws IOException

    /**
     * Return whether the uploaded fileInputBean is empty, that is, either no fileInputBean has
     * been chosen in the multipart form or the chosen fileInputBean has no content.
     */
    boolean isEmpty()

    /**
     * Return the size of the fileInputBean in bytes.
     * @return the size of the fileInputBean, or 0 if empty
     */
    long getSize()

    /**
     * Return the contents of the fileInputBean as an array of bytes.
     * @return the contents of the fileInputBean as bytes, or an empty byte array if empty
     * @throws IOException in case of access errors (if the temporary store fails)
     */
    byte[] getBytes() throws IOException

    /**
     * Return an InputStream to read the contents of the fileInputBean from.
     * The user is responsible for closing the stream.
     * @return the contents of the fileInputBean as stream, or an empty stream if empty
     * @throws IOException in case of access errors (if the temporary store fails)
     */
    InputStream getInputStream() throws IOException

    /**
     * Method to check if file exists on disk or not
     * @return
     */
    boolean isExists()
}
