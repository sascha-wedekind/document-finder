## Build the image
TO build the image execute
```shell
.\gradlew jlink
```
this will generate an image in the directory `[PROJECT_DIR]/build/image/`.
Read each error message during build carefully and adapt the `jlink/mergedModule` configuration in the `build.gradle` file.

##Test if the build image runs
Got to the directory `[PROJECT_DIR]/build/image/bin` and start the application with
```shell
.\java.exe --module DocumentFinder.main/com.bytedompteur.documentfinder.DocumentFinderMain
```
and test all features of the application. If any service could not be found during runtime an error stack trace will be printed on the console.


## View module-info created by org.beryx.jlink plugin
Run task `createMergedModule`. This creates the file 
`[PROJECT_DIR]/build/jlinkbase/tmpjars/com.bytedompteur.merged.module/module-info.jar`

Note: The directory and the field `module-info.java` will only persist if the gradle task `createMergedModule` is executed. If `jlink` is executed, the directory above will be deleted.


## List modules contained in java image created by jlink
Got to the directory `[PROJECT_DIR]/build/image/bin` and
```shell
./java --list-modules
```

## List module content in java image created by jlink
Got to the directory `[PROJECT_DIR]/build/image/bin` and
```shell
./java --describe-module com.bytedompteur.merged.module
```

## Run jlink generated java package in debug mode
Ensure that the java module `jdk.jdwp.agent` is contained in the image (Gradle::jlink::mergeModule) 
```shell
./java '-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=*:5005' --module DocumentFinder.main/com.bytedompteur.documentfinder.DocumentFinderMain
```
