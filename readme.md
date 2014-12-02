# Dropbox4S
This is a Scala library for the [Dropbox API](https://www.dropbox.com/developers). <br/>
Dropbox4S supports [Core API](https://www.dropbox.com/developers/core) and [Datastore API](https://www.dropbox.com/developers/datastore).<br/>
Supported Scala version are 2.10.x and 2.11.x


## Install
Add the following dependencies to build.sbt.<br/>
```Scala
resolvers += "bintray" at "http://dl.bintray.com/shinsuke-abe/maven"

libraryDependencies += "com.github.Shinsuke-Abe" %% "dropbox4s" % "0.2.0"
```


## Dependencies with other libraries
Dropbox4S has dependencies with the following libraries:<br/>

* dispatch 0.11.0
* json4s(native) 3.2.10
* dropbox-core-sdk 1.7.6

Note: These libraries are the latest stable releases (November 3rd, 2014).


## How to use
Dropbox4S supports Core API and Datastore API.<br/>


### Using Core API
The core API of Dropbox4S is a DSL for dropbox-core-sdk (the java SDK).<br/>
The return values of the DSL are dropbox-core-sdk's classes.
For more detail about these classes, see [the official dropbox sdk documentation](http://dropbox.github.io/dropbox-sdk-java/api-docs/v1.7.x/).<br/>


First, extends the `dropbox4s.core.CoreApi` trait and implements some of the required fields. Be sure to define the `val auth: DbxAuthFinish` as implicit.

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
The Core API requires a client identifier for each request. Libraries based on the basic SDK should append this identifier.<br/>
With Dropbox4S, it's done fore you. The library identifier added to each request is "dropbox4s/0.2.0".<br/>
For example, if your application identifier is "my_file_apps/1.0.0" (defined using the `applicationName` and `version` values),
Dropbox4S will send the identifier "my_file_apps/1.0.0 dropbox4s/0.2.0" to the API.<br/>


#### Core API DSL

Implicit conversions for some classes are implemented in `dropbox4s.core.CoreApi` trait.
Core API DSL is provided by these conversions.


##### Dropbox Path

To demonstrate either a local path or a remote one on dropbox, the Core API DSL has a `dropbox4s.core.model.DropboxPath` class.
The parts of the API operating on dropbox files accept a path as a `DropboxPath` class.

Create DropboxPath class.
```Scala
val appPath = DropboxPath("/yourapplicationpath")
```

Root directory on DropboxPath is your application root directory.

Add child for DropboxPath.
```Scala
val addChildPath = appPath / "childdir"
```

DropboxPath has parent and name methods.
```Scala
addChildPath.parent // returns "yourapplicationpath"
addChildPath.name   // returns "childdir" 
```

Only create new path for `DropboxPath` instance, not applied on dropbox.
Use `createFolder` method to apply it on dropbox.
```Scala
createFolder(addChildPath)
```


##### Uploading File

Uploading a local file to dropbox for the first time.
```Scala
val localFile = new java.io.File("your local file ptah")
val uploadedFile = localFile uploadTo addChildPath
```

`uploadTo` method returns `com.dropbox.core.DbcEntry.File` instance.

If a file already exists with the same path on dropbox, the API creates a copy with an added suffix number.
To update a file on dropbox, you can use the "updating file APIs" of Dropbox4s. If you want to overwrite the remote file with the uploaded one, set the `isForced` parameter to true (default, false).
```Scala
// upload file forced
val uploadedFile = localFile uploadTo(addChildPath, true)
```

If you want to upload a big file, you will need to use the chunked uploads functionality. To do so, set `Some(Int)` value to the `chunkSize` parameter (default, `None`).
If you set this parameter, dropbox4s will use the chunked upload API.
```
// for chuncked upload, chunk size is 10kb.
val uploadedFile = localFile uploadTo(addChildPath, chunkSize = Some(10240))
```

`chunkSize` is bytes number.
For more information about the chunked update API, see the [official blog post](https://www.dropbox.com/developers/blog/21/chunked-uploads-beta).


##### Updating File

To update a file on dropbox, use the implicit conversions for `com.dropbox.core.DbxEntry.File`.
```Scala
val forUpdateFile = new java.io.File("your local file path")
uploadedFile update forUpdateFile
```

If you want to update a big file, you will need to use the chunked uploads functionality. To do so, set `Some(Int)` value to the `chunkSize` parameter (default, `None`).
If you set this parameter, dropbox4s will use the chunked upload API.
```Scala
// for chunked upload, chunk size is 10kb.
uploadedFile update(forUpdateFile, Some(10240))
```

`chunkSize` is bytes number.
For more information about the chunked update API, see the [official blog post](https://www.dropbox.com/developers/blog/21/chunked-uploads-beta).

##### Downloading File

Use the `downloadTo` method to download a file from dropbox.
`DropboxPath` or `DbxEntry.File` classes have implicit conversion.
```Scala
// use DropboxPath
addChildPath downloadTo "your local path"

// use DbxEntry.File
uploadedFile downloadTo "your local path"
```

##### Listing folders and search files

Use `children` method to get the metadata of children under a dropbox path.
```Scala
// list uploaded files and folders under addChildPath
val children = addChildPath children
```

Use `search` method to search files and folders under a dropbox path, contains substring.
```Scala
// search files contains "foo" substring under addChildPath
val hasFooFiles = search(addChildPath, "foo")
```

##### Other operations for File on Dropbox

```Scala
val copyTarget = DropboxPath("copy file path")

// copy file to another dropbox path(DbxEntry.File)
uploadedFile copyTo copyTarget

// or folder, use copyTo method on DropboxPath
addChildPath copyTo copyTarget

val moveTarget = DropboxPath("move file path")

// move file to another dropbox path(DbxEntry.File)
// or folder, use moveTo method on DropboxPath
val movedFile = uploadedFile moveTo moveTarget

// remove file from dropbox(DbxEntry.File)
movedFile remove

// remove folder from dropbox(DropboxPath)
addChildPath remove
```

If you want to copy file across directories, use the `copyRef` method.
```Scala
// get copy ref for get file copy other user(DbxEntry.File).
// or folder, use copyRef method on DropboxPath
val ref = uploadedFile copyRef

// in other user's application, copy file to user's dropbox path by using copy ref.
// or folder, use copyFrom method on DropboxPath
uploadedFile copyFrom ref
```

Core API has other methods, `thumbnail`, `restore`, `shareLink`, `tempDirectLink`, `revision`, `accountInfo`.
See scaladoc(under `doc` directory on repository) for details.


### Using Datastore API
Datastore API of Dropbox4S is written in Scala, without using the base sdk.<br/>


Import `dropbox4s.datastore.DatastoresApi` object for using Datastore API DSL and DbxAuthFinish instance set to implicit value.
```Scala
class YourApplicataion {
  import DatastoresApi._

  // user's authenticate information
  implicit val auth: DbxAuthFinish = webAuth.finish // how to get oauth access token, see dropbox-core-sdk document.

  // application code
}
```

#### Datastore API DSL

#### Creating a datastore

When creating a local datastore account, use the `get` method and pass `orCreate` value as a second parameter. 

```Scala
val datastore = get("datastorename", orCreate)
```

`orCreate` is a value predefined by DatastoreApi object. Actually, it's a boolean defaulting to `true`.

If you set the second parameter to `true`, `get` method will try to create the datastore if a datastore with the same name doesn't already exist.
If you set it to `false`, it will only try to get the datastore.
The default value of the second parameter is `false`.

To create a shareable datastore, use the `createShareable` method.

```Scala
val sharedDatastore = createShareable("youappname")
```

This datasotre is sharing data across multiple Dropbox accounts.

Note: Key of shareable datastore are unique across Dropbox.

To check if a datastore is shared, use the `isShareable` method on the object that both `get` and `createShareable` methods are returning.

```Scala
datastore.isShareable // false
sharedDatastore.isShareable // true
```

To delete a datastore, use the `delete` method.

```Scala
datastore.delete
```

#### Roles(for shareable datastore)

Any shareable datastores has an access control list.
To get the assigned role, use the `assignedRole` method.

```Scala
val role = sharedDatastore.assignedRole
```

To assign a role, use the `assign` method.

```Scala
sharedDatastore.assign(Viewer to Public)
```

`Viewer` and `Public` are role or principle object. Write `Role to Principle`, create datastore access control record. 

Drop role for principle, use `withdrowRole` method.

```Scala
sharedDatastore.withdrawRole(Public)
```

See the 'Shared datastores' section of [the official documentation](https://www.dropbox.com/developers/datastore/docs/http), for details about principle and roles.
Dropbox account creating shared datastore, set role is 'Owner'.

#### Listing datastores

To see what datastores exist within an account, call `listDatastores` method.

```Scala
val list = listDatastores
```

#### Getting a snapshot

To get a snapshot of the current content of a datastore, call `snapshot` method.

```Scala
val snapshot = get("datastorename").snapshot
```

Snapshot has `tableNames` and `table` methods.
To get the name list of tables of a snapshot, call `tableNames` method.

```Scala
val names = snapshot.tableNames
```

#### Getting table and record

To get a table of the current contents, call the `table` method and provide a converter function. This function should convert scala objects to json values.
Json value is `JValue` class, this class is provided by json4s.

Note: Field type of record class must be below types.

* `Int`
* `Boolean`
* `String`
* `WrappedBytes`
* `WrappedInt`
* `WrappedSpecial` is implementing to `PlusInf` or `MinusInf` or `Nan` objects
* `WrappedTimestamp`
* `List` or `Either` or `Option` of above classes

```Scala
case class SampleRow(name: String, price: Int)

val converter = SampleRow => JValue = (data) => {
  ("name" -> data.name) ~ ("price" -> data.price)
}

val sampleTable = snapshot.table("SampleRow")(converter)
```

The `table` method returns a `Table` object and has a `rows` field.
This field contains the mapped data and has the same tid(table id) than in the current snapshot.

`Table` object has some method for operating on rows.

```Scala
// To insert new records with set rowid for identify data
table.insert(TableRow("rowidfoo", SampleRow("foo", 100)), TableRow("rowidbar", SampleRow("bar", 200)))

// To get single row, parameter is rowid
sampleTable.get("rowidfoo")

// To get multi rows filtered by condition
table.select(data => data.price > 300)

// To update record by rowid
table.update("rowidfoo", SampleRow("foo", 350))

// To update records by condition
// First parameter is value update rule, second parameter is condition
table.update(data => data.copy(price = price * 1.08))(row => row.price < 100)

// To delete records by rowids
table.delete("rowidfoo", "rowidbar")

// To delete records by condition
table.delete(row => row.name == "bar")

// To truncate rows of table
table.truncate
```

See scaladoc(under `doc` directory on repository) for details.

## About function addition for DSL
New functions will be added for new version.<br/>
If you want any other DSL, add issue or send mention to @mao_instantlife on Twitter please.
