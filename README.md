# File Uploader Plugin
#### Causecode Technologies Pvt. Ltd.

## Access Protect Controller & Actions

```
'/fileUploader/show': ['ROLE_USER'], (According to application needs)
'/fileUploader/upload': ['ROLE_USER'] (According to application needs)
'/fileUploader/download': ['ROLE_USER'] (According to application needs)
'/fileUploader/deleteFile': ['ROLE_USER'] (According to application needs)
'/fileUploader/*': ['ROLE_ADMIN'] (CRUD actions needs to secured)

```

## ChangeLog

### Version 2.1

#### Improvements

1. Added `/fileUploader/show/$id` to render any image to hide actual directory path of the file,
2. Added List page for admins to see list of files,
3. Deleting a UFile instance will automatically delete the physical file.
4. Receiving an empty or blank file will not throw an exception, instead it simply returns null value. Useful to directly write service irrespective of nullable constraints of the field.

### Version 2.0.11

1. You can clone a file by passing the instance of existing UFile. See `cloneFile` method in service.
