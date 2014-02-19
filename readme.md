# Dropbox4S
This is a Scala library of [Dropbox API](https://www.dropbox.com/developers). <br/>
Dropbox4S supports [Core API](https://www.dropbox.com/developers/core) and [Datastore API](https://www.dropbox.com/developers/datastore).<br/>
Supported Scala version is 2.10.x.


## Install
Add the following dependencies to build.sbt.
```
resolvers += "bintray" at "http://dl.bintray.com/shinsuke-abe/maven"
libraryDependencies += "com.github.Shinsuke-Abe" %% "dropbox4s" % "0.1.0"
```


## Dependencies with other libraries
Dropbox4S has dependencies with following libraries.
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
```
class YourApplication extends CoreApi {
  // implements fields
  val applicationName = "YourApplicationName"
  val version = "1.0.0" // your application version(string)
  val locale = Locale.getDefault


  // user's access token
  implicit val token = AccessToken("user token")


  // application code
}
```
Note: Core API requires client identifier on request.<br/>


#### Core API DSL
In preparation.


### Using Datastore API
Datastore API of Dropbox4S is written by Scala. Without base sdk.<br/>
Return values defined on this library.<br/>


Import dropbox4s.datastore.DatastoresApi object for using Datastore API DSL and access token set to implicit value.

```
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
