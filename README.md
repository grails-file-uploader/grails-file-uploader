# File-Uploader Plugin (Latest 2.4.2-RC2)

#### Causecode Technologies Pvt. Ltd.

## Access Protect Controller & Actions

```
'/fileUploader/show': ['ROLE_USER'], (According to application needs)
'/fileUploader/download': ['ROLE_USER'], (According to application needs)
```

## Uploading files to CDN

To upload files to CDN (Supports both Rackspace and Amazon) one must have some configuration like given below:

```
import com.lucastex.grails.fileuploader.CDNProvider

grails.tempDirectory = "./temp-files"     // Required to store files temporarily. Must not ends with "/"

fileuploader {
    RackspaceKey = "mykey"
    RackspaceUsername = "myusername"

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