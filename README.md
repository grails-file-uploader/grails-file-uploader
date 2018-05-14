# File-Uploader Plugin (Latest 3.2.0)

[![Maintainability](https://api.codeclimate.com/v1/badges/13bfee73c29ecd2ea4b2/maintainability)](https://codeclimate.com/github/causecode/grails-file-uploader/maintainability)
[![Test Coverage](https://api.codeclimate.com/v1/badges/13bfee73c29ecd2ea4b2/test_coverage)](https://codeclimate.com/github/causecode/grails-file-uploader/test_coverage)

File-Uploader Plugin > 3.2.0 supports Grails 3.3.x
File-Uploader Plugin < 3.1.2 supports Grails 3.2.x (Tested upto 3.2.x)

# For Grails 2.x refer branch [here](https://github.com/causecode/grails-file-uploader/tree/grails-2.x-master)

#### Causecode Technologies Pvt. Ltd.

## Access Protect Controller & Actions

```
'/file-uploader/show': ['ROLE_USER'], (According to application needs)
'/file-uploader/download': ['ROLE_USER'], (According to application needs)
```

## Uploading files to CDN

To upload files to CDN (Supports both Rackspace and Amazon) one must have some configuration like given below:

```
import com.causecode.fileuploader.CDNProvider

grails.tempDirectory = "./temp-files"     // Required to store files temporarily. Must not ends with "/"

fileuploader {

    storageProvider {

        amazon {
            AmazonKey = "somekey"	// For amazon S3
            AmazonSecret = "somesecret"
            defaultContainer = "anyConatainer"  // Container to move local files to cloud
        }
        google {
            authFile = '/path/to/key.json'

            // This is must for both cases, i.e reading file using the path in 'auth' or reading hard coded credentials from here itself.
            project_id = '<project_id_provided_in_json_key_file>'

            // Other required values from JSON key file.
            private_key_id = '<private_key_id_provided_in_json_key_file>'
            private_key = '<private_key_provided_in_json_key_file>'
            client_email = '<client_email_provided_in_json_key_file>'
            client_id = '<client_id_provided_in_json_key_file>'

            // Optional, this defaults to 'service_account'.
            type = ''
        }
    }

    groups {
        degreeApplication {			// Non CDN files, will be stored in local directory.
            maxSize = 1000 * 1024 //256 kbytes
            allowedExtensions = ["xls"]
            path = "./web-app/degree-applications"
            storageTypes = ""
            //Explnation about this congiguration is given below
            checksum {
                calculate = true
                algorithm = Algorithm.MD5                
            }
        }
        userAvatar {
            maxSize = 1024 * 1024 * 2 //256 kbytes
            allowedExtensions = ["jpg","jpeg","gif","png"]
            storageTypes = "CDN"
            container = "anyContainerName"
            checksum {
                calculate = false
                algorithm = Algorithm.SHA1                
             }
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
}
```

To enable CDN to any group you must have a [rackspace](http://docs.rackspace.com/) account with a username & key.
This username & key needs to be passed in config as shown in above example. Authentication can be done using username
& password pair but currently only key/username pair is supported.    

1. To enable CDN uploading to any group just set **storageType** to **CDN** & provide a container name.

2. By default path URL retrieved from Amazon S3 service is temporary URL, which will be valid for 30 days bydefault. Which
can be overwritten for group level configuration by setting **expirationPeriod**. This period must be of long type in seconds.

3. For Google Cloud Authentication, you will have to add the key (JSON file downloaded from the Cloud Console) to the server and add an environment
   variable called GOOGLE_APPLICATION_CREDENTIALS which points to the file. In bashrc file, add:
   ```
   # Google Default Credentials
   export GOOGLE_APPLICATION_CREDENTIALS='/path/to/key.json'
   ```
4. To Enable checksum checks and generation, define configuration as shown in the sample configuration, 
```
 groups {
        degreeApplication {			// Non CDN files, will be stored in local directory.
            maxSize = 1000 * 1024 //256 kbytes
            allowedExtensions = ["xls"]
            path = "./web-app/degree-applications"
            storageTypes = ""
            checksum {
                calculate = true
                algorithm = Algorithm.MD5                
            }
        }
  }

``` 
If first flag is set to true, plugin will generate checksum for the uploaded file and try to find a file from database having same checksum. If any such file is found, then plugin will throw an **DuplicateFileException** exception.
Second flag will tell plugin which algorithm to use to calculate the checksum. Currently, possible choices are,
``` 
Algorithm.MD5 and,
Algorithm.SHA1
```
Second flag will be ignored if first flag is set to false. By default checksum calculation features is disabled. To enable provide valid configurations. 

5. To disable the auto renew URL job from the installing app, add the following config in the application.groovy file.
```
jobs {
    fileUploader.renewURLs.disable = true // By default it is set to false
}
```

6. To make files public add makePublic: true to group config in application.groovy.
Note: Public URLs do not expire.
