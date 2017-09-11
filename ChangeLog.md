# Change Log

## Version 3.0.7

1. Upgraded `google-cloud-storage` dependency version to `1.4.0`. [#9](https://bitbucket.org/causecode/grails-file-uploader/issues/9/upgrade-google-cloud-storage-dependency-to)
2. Made changes in `GoogleCDNFileUploaderImpl` and `GoogleCredentials` files as per the `google-cloud-storage` API.
3. Renamed the method `setAuthCredentialsAndAuthenticate` to `setCredentialsAndAuthenticate` in class `GoogleCredentials`.
It now accepts a [`com.google.auth.Credentials`](https://github.com/google/google-auth-library-java/blob/master/credentials/java/com/google/auth/Credentials.java) object as an argument. Before the change it was accepting
`com.google.cloud.AuthCredentials`, which no longer exists.

## Version 3.0.6

1. Added a renew action to be triggered in case the job fails

## Version 2.4.3

1. Added support for configurable public URL for Amazon S3
2. Adding cache header to all files being uploaded (if configured)
3. One time method to update cache header for all existing files in the Amazon S3 bucket
4. Added codenarc for fixing code errors

## Version 2.4.2-RC1

1. Various code cleanup
2. Added support for CDN based UFile cloning
3. Rackspace credentials renamed to **RackspaceUsername** and **RackspaceKey**

## Version 2.4-RC2, 2.4.1

### New Features

1. Added temporary URL feature for Amazon S3 file uploader,
2. Fixed deleting cloud files when not using production environment,
3. Temporary directory made configurable instead hard coded value to use. (Use **grails.tempDirectory**),

### Database Changes

1. Added **expireOn** field in UFile domain.

## Version 2.4-RC1

## Database Changes

1. **type** field will be now of type integer.

## Improvement

1. Using StringBuilder for file name creation for CDN files.
2. Removed file extension from file name. (#8).

## Version 2.4-SNAPSHOT

## Improvement

1. Upgraded apache jcloud jar dependency from incubating to stable version.

### New Features

1. Added support for uploading files to Amazon S3 provider.

### Database changes

1. Added provider field in UFile domain

## Version 2.3, 2.3.1

## Improvement

1. Fixed jar dependency resolution,
2. Appending environment name to the container name in other then production environment,
3. Retrieving ssl url of file if app server url is ssl.

## Version 2.2

### Database changes

1. Added **type** field in UFile domain. Must have default value 'LOCAL'

### New Features

1. One can now upload files to Cloud CDN, if configured the group. See below for details.

## Version 2.1

### Improvements

1. Added `/fileUploader/show/$id` to render any image to hide actual directory path of the file,
2. Added List page for admins to see list of files,
3. Deleting a UFile instance will automatically delete the physical file.
4. Receiving an empty or blank file will not throw an exception, instead it simply returns null value. Useful to directly write service irrespective of nullable constraints of the field.

## Version 2.0.11

1. You can clone a file by passing the instance of existing UFile. See `cloneFile` method in service.
