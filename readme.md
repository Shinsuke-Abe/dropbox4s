# Dropbox4S
This is a Scala library of [Dropbox API](https://www.dropbox.com/developers). <br/>
Dropbox4S supports [Core API](https://www.dropbox.com/developers/core) and [Datastore API](https://www.dropbox.com/developers/datastore).<br/>
Supported Scala version is 2.10.x.


## Install
In preparation.<br/>
If you use beta version, please build these code and publish local.


## Dependencies with other libraries
Dropbox4S has dependencies with following libraries.<br/>

* dispatch 0.11.0
* json4s(native) 3.2.6
* dropbox-core-sdk 1.7.6

Note: These libraries on latest stable at Feburary 18,2014.


## How to use
Dropbox4S supports Core API and Dropbox API.<br/>


### Using Core API
Core API of Dropbox4S is DSL for dropbox-core-sdk.<br/>
Return value of DSL is dropbox-core-sdk's classes. Detail these classes, see [official documents](http://dropbox.github.io/dropbox-sdk-java/api-docs/v1.7.x/)<br/>


First, trait dropbox4s.core.CoreApi mixin to your application class and implements some fields and access token set to implicit value.
```Scala
class YourApplication extends CoreApi {
  // implements fields
  val applicationName = "YourApplicationName"
  val version = "1.0.0" // your application version(string)
  override val locale = Locale.JAPANESE // if customize local, override local field


  // user's access token
  implicit val token = AccessToken("user token")


  // application code
}
```
Note:<br/>
Core API requires client identifier on request. Higher library on basic SDK should append library identifier.<br/>
On Dropbox4S, append library identifier "dropbox4s/0.1.0" to your identifier.<br/>
For example, if your application identifier is "my_file_apps/1.0.0",
Dropbox4S send identifier "my_file_apps/1.0.0 dropbox4s/0.1.0" to API.<br/>



#### Core API DSL
In preparation.


### Using Datastore API
Datastore API of Dropbox4S is written by Scala. Without base sdk.<br/>
Return values defined on this library.<br/>


Import dropbox4s.datastore.DatastoresApi object for using Datastore API DSL and access token set to implicit value.

```Scala
class YourApplicataion {
  import DatastoresApi._

  // user's access token
  implicit val token = AccessToken("user token")

  // application code
}
```


#### Datastore API DSL
In preparation.


## About function addition for DSL
New functions will be added for new version.<br/>
If you want any other DSL, add issue or send mention to @mao_instantlife on Twitter please.
