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
