# File Uploader Plugin
#### Causecode Technologies Pvt. Ltd.

## Access Protect Controller & Actions

```
'/fileUploader/show': ['ROLE_USER'], (According to application needs)
'/fileUploader/upload': ['ROLE_USER'], (According to application needs)
'/fileUploader/download': ['ROLE_USER'], (According to application needs)
'/fileUploader/deleteFile': ['ROLE_USER'], (According to application needs)
```

## ChangeLog

### Version 3.0

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
fileuploader {
    CDNUsername = "myusername"
    CDNKey = "mykey"
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
}
```

To enable CDN to any group you must have a [rackspace](http://docs.rackspace.com/) account with a username & key.
This username & key needs to be passed in config as shown in above example. Authentication can be done using username
& password pair but currently only key/username pair is supported.    

To enable CDN uploading to any group just set **storageType** to **CDN** & provide a container name.