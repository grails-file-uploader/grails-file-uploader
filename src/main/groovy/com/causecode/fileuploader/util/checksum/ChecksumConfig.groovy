/*
 * Copyright (c) 2018, CauseCode Technologies Pvt Ltd, India.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or
 * without modification, are not permitted.
 */

package com.causecode.fileuploader.util.checksum

import groovy.transform.Canonical
import groovy.transform.ToString

/**
 * Checksum Config object to take decision weather to calculate checksum/hash or not, and if to calculate then with which algorithm
 * @author Milan Savaliya
 * @since 3.1.0
 */

@Canonical
@ToString(includeNames = true)
class ChecksumConfig {
    boolean calculate = false
    Algorithm algorithm = Algorithm.MD5
}
