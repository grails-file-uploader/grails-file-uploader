package com.causecode.fileuploader

/**
 * This class is used for mapping requests to controllers and views.
 */
class UrlMappings {

	static mappings = {
		"/$controller/$action?/$id?" {
			constraints {
				// apply constraints here
			}
		}
	}
}
