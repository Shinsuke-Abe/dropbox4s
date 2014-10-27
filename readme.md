# Dropbox4S
This is a Scala library of [Dropbox API](https://www.dropbox.com/developers). <br/>
Dropbox4S supports [Core API](https://www.dropbox.com/developers/core) and [Datastore API](https://www.dropbox.com/developers/datastore).<br/>
Supported Scala version is 2.10.x and 2.11.x


## Install
Add the following dependencies to build.sbt.<br/>
```Scala
resolvers += "bintray" at "http://dl.bintray.com/shinsuke-abe/maven"
libraryDependencies += "com.github.Shinsuke-Abe" %% "dropbox4s" % "0.2.0"
```


## Dependencies with other libraries
Dropbox4S has dependencies with following libraries.<br/>

* dispatch 0.11.0
* json4s(native) 3.2.10
* dropbox-core-sdk 1.7.6

Note: These libraries on latest stable at October 21,2014.


## How to use
Dropbox4S supports Core API and Datastore API.<br/>


### Using Core API
Core API of Dropbox4S is DSL for dropbox-core-sdk.<br/>
Return value of DSL is dropbox-core-sdk's classes.
Detail these classes, see [official documents](http://dropbox.github.io/dropbox-sdk-java/api-docs/v1.7.x/).<br/>


First, trait `dropbox4s.core.CoreApi` mixin to your application class and implements some fields and DbxAuthFinish instance set to implicit value.
```Scala
class YourApplication extends CoreApi {
  // implements fields
  val applicationName = "YourApplicationName"
  val version = "1.0.0" // your application version(string)
  override val locale = Locale.JAPANESE // if customize local, override local field


  // user's authenticate information
  implicit val auth: DbxAuthFinish = webAuth.finish // how to get oauth access token, see dropbox-core-sdk document.


  // application code
}
```
Note:<br/>
Core API requires client identifier on request. Higher library on basic SDK should append library identifier.<br/>
On Dropbox4S, append library identifier "dropbox4s/0.2.0" to your identifier.<br/>
For example, if your application identifier is "my_file_apps/1.0.0",
Dropbox4S send identifier "my_file_apps/1.0.0 dropbox4s/0.2.0" to API.<br/>


#### Core API DSL

Implicit conversions for some classes are implemented in `dropbox4s.core.CoreApi` trait.
Core API DSL is provided by these conversions.


##### Dropbox Path

To demonstrate either path on local or dropbox, Core API DSL has `dropbox4s.core.model.DropboxPath` class.
API operate dropbox files accepts path as DropboxPath class.

Create DropboxPath class.
```Scala
val appPath = DropboxPath("/yourapplicationpath")
```

Root directory on DropboxPath is your application root directory.

Add child for DropboxPath.
```Scala
val addChildPath = appPath / "childdir"
```

DropboxPath has parent and name method.
```Scala
addChildPath.parent // returns "yourapplicationpath"
addChildPath.name   // returns "childdir" 
```

Only create new path for `DropboxPath` instance, not applied on dropbox.
Use `createFolder` method to apply on dropbox.
```Scala
createFolder(addChildPath)
```


##### Uploading File

A file on local machine upload to dropbox, first time.
```Scala
val localFile = new java.io.File("your local file ptah")
val uploadedFile = localFile uploadTo addChildPath
```

`uploadTo` method returns `com.dropbox.core.DbcEntry.File` instance.

If a file already in same path on dropbox, API create a copy added prefix number.
Use updating file APIs for update files on dropbox, or uplaod file forced set `isForced` parameter to true (default, false).
```Scala
// upload file forced
val uploadedFile = localFile uploadTo(addChildPath, true)
```

In use to `isForced` parameter, if a file already in same path on dropbox, API update a exist file.

On upload to big size file, set `Some(Int)` value to `chunkSize` parameter(default, `None`).
If set this parameter, dropbox4s uses chunked upload API.
```
// for chuncked upload, chunk size is 10kb.
val uploadedFile = localFile uploadTo(addChildPath, chunkSize = Some(10240))
```

`chunkSize` is bytes number.
For other information of chunked update API, see the [official blog post](https://www.dropbox.com/developers/blog/21/chunked-uploads-beta).


##### Updating File

To update a file on dropbox, using implicit conversions for `com.dropbox.core.DbxEntry.File`.
```Scala
val forUpdateFile = new java.io.File("your local file path")
uploadedFile update forUpdateFile
```

On update to big size file, set `Some(Int)` value to `chunkSize` parameter(default, `None`)
If set this paramter, dropbox4s uses chunked upload API.
```Scala
// for chunked upload, chunk size is 10kb.
uploadedFile update(forUpdateFile, Some(10240))
```

`chunkSize` is bytes number.
For other information of chunked update API, see the [official blog post](https://www.dropbox.com/developers/blog/21/chunked-uploads-beta).


##### Downloading File

Use `downloadTo` method to download file on dropbox.
`DropboxPath` or `DbxEntry.File` classes has implicit conversion. And use this method.
```Scala
// use DropboxPath
addChildPath downloadTo "your local path"

// use DbxEntry.File
uploadedFile downloadTo "your local path"
```

##### Listing folders and search files

Use `children` method to get metadata of children under dropbox path.
```Scala
// list uploaded files and folders under addChildPath
val children = addChildPath children
```

Use `search` method to search files and folders under dropbox path, contains substring.
```Scala
// search files contains "foo" substring under addChildPath
val hasFooFiles = search(addChildPath, "foo")
```

##### Other operations for File on Dropbox

remove
copyTo
moveTo

copyRef
copyFrom

thumbnail
restore
shareLink
tempDirectLink


### Using Datastore API
Datastore API of Dropbox4S is written by Scala. Without base sdk.<br/>
Return values defined on this library.<br/>


Import dropbox4s.datastore.DatastoresApi object for using Datastore API DSL and DbxAuthFinish instance set to implicit value.
```Scala
class YourApplicataion {
  import DatastoresApi._

  // user's authenticate information
  implicit val auth: DbxAuthFinish = webAuth.finish // how to get oauth access token, see dropbox-core-sdk document.

  // application code
}
```


#### Datastore API DSL
In preparation.<br/>
[Test code](https://github.com/Shinsuke-Abe/dropbox4s/blob/master/src/test/scala/dropbox4s/datastore/DatastoresApiTest.scala) for sample.

## About function addition for DSL
New functions will be added for new version.<br/>
If you want any other DSL, add issue or send mention to @mao_instantlife on Twitter please.
