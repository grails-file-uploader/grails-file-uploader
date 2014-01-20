# File Uploader Plugin (Latest 2.4-RC1)

#### Causecode Technologies Pvt. Ltd.

## Access Protect Controller & Actions

```
'/fileUploader/show': ['ROLE_USER'], (According to application needs)
'/fileUploader/upload': ['ROLE_USER'], (According to application needs)
'/fileUploader/download': ['ROLE_USER'], (According to application needs)
'/fileUploader/deleteFile': ['ROLE_USER'], (According to application needs)
```

## ChangeLog

### Version 2.4-RC2

#### New Features

1. Added temporary URL feature for Amazon S3 file uploader.

#### Database Changes

1. Added **expireOn** field in UFile domain.

### Version 2.4-RC1

### Database Changes

1. **type** field will be now of type integer.

### Improvement

1. Using StringBuilder for file name creation for CDN files.
2. Removed file extension from file name. (#8).

### Version 2.4-SNAPSHOT

### Improvement

1. Upgraded apache jcloud jar dependency from incubating to stable version.

#### New Features

1. Added support for uploading files to Amazon S3 provider.

#### Database changes

1. Added provider field in UFile domain

### Version 2.3, 2.3.1

### Improvement

1. Fixed jar dependency resolution,
2. Appending environment name to the container name in other then production environment,
3. Retrieving ssl url of file if app server url is ssl.

### Version 2.2

#### Database changes

1. Added **type** field in UFile domain. Must have default value 'LOCAL'

#### New Features

1. One can now upload files to Cloud CDN, if configured the group. See below for details.

### Version 2.1

#### Improvements

1. Added `/fileUploader/show/$id` to render any image to hide actual directory path of the file,
2. Added List page for admins to see list of files,
3. Deleting a UFile instance will automatically delete the physical file.
4. Receiving an empty or blank file will not throw an exception, instead it simply returns null value. Useful to directly write service irrespective of nullable constraints of the field.

### Version 2.0.11

1. You can clone a file by passing the instance of existing UFile. See `cloneFile` method in service.


## Uploading files to CDN

To upload files to CDN (Only Supports Rackspace now) one must have some configuration like given below:

```
import com.lucastex.grails.fileuploader.CDNProvider

fileuploader {
    CDNUsername = "myusername"
    CDNKey = "mykey"
    AmazonKey = "somekey"	// For amazon S3
    AmazonSecret = "somesecret"
    defaultContainer = "anyConatainer"  // Container to move local files to cloud

    degreeApplication {			// Non CDN files, will be stored in local directory.
        maxSize = 1000 * 1024 //256 kbytes
        allowedExtensions = ["xls"]
        path = "./web-app/degree-applications"
        storageTypes = ""
    }
    userAvatar {
        maxSize = 1024 * 1024 * 2 //256 kbytes
        allowedExtensions = ["jpg","jpeg","gif","png"]
        storageTypes = "CDN"
        container = "anyContainerName"
    }
    logo {
        maxSize = 1024 * 1024 * 2 //256 kbytes
        allowedExtensions = ["jpg","jpeg","gif","png"]
        storageTypes = "CDN"
        container = "anyContainerName"
        provider = CDNProvider.AMAZON
        expirationPeriod = 60 * 60 * 24 * 2 // Two hours
    }
}
```

To enable CDN to any group you must have a [rackspace](http://docs.rackspace.com/) account with a username & key.
This username & key needs to be passed in config as shown in above example. Authentication can be done using username
& password pair but currently only key/username pair is supported.    

1. To enable CDN uploading to any group just set **storageType** to **CDN** & provide a container name.

2. By default path URL retrieved from Amazon S3 service is temporary URL, which will be valid for 30 days bydefault. Which
can be overwritten for group level configuration by setting **expirationPeriod**. This period must be of long type in seconds.