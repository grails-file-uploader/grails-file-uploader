package com.causecode.fileuploader.util.checksum

import groovy.transform.Canonical
import groovy.transform.ToString

/**
 * Checksum Config object to take decision weather to calculate and if calculate then with which algorithm
 * @author Milan Savaliya
 */

@Canonical
@ToString(includeNames = true)
class ChecksumConfig {
    boolean calculate = false
    Algorithm algorithm = Algorithm.MD5
}
