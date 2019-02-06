package com.causecode.fileuploader.util.checksum

/**
 * Checksum Config object to take decision weather to calculate checksum/hash or not, and if to calculate then with
 * which algorithm
 * @author Milan Savaliya
 * @since 3.1.0
 */

class ChecksumConfig {
    boolean calculate = false
    Algorithm algorithm = Algorithm.MD5
}
