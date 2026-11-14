## What the AOT cache does
The JDK AOT cache (JEP 483) stores the classes the application loads during a start in a file. The next start reads the
classes from that file instead of loading and verifying them from the jars. On macOS arm64 this shortens the time until
the application is started from about 1.8 seconds to about 1.4 seconds (about 22%).

The cache is optional. Without it the application behaves exactly as before.

## Build an app image or installer with the cache
Add the project property `aotCache` to a build that creates the jpackage app image:
```shell
./gradlew jpackageImage -PaotCache
./gradlew jpackage -PaotCache
```
The installer is built from the app image, so the cache is part of the `.dmg`, `.msi` or `.deb` too. It adds about 60 MB
to the application.

The build script is `gradle/aot-cache.gradle`. After the app image is created it
1. starts the application with `-XX:AOTMode=record` and the system property `documentfinder.aot.training=true`. The
   application then leaves on its own two seconds after the main window was shown,
2. starts the application again with `-XX:AOTMode=create`, which writes the cache `document-finder.aot` next to the jars
   (`Contents/app` on macOS, `app` on Windows, `lib/app` on Linux),
3. adds `java-options=-XX:AOTCache=$APPDIR/document-finder.aot` to the launcher configuration (`*.cfg`),
4. signs the app bundle ad hoc again on macOS, because changing its content invalidates the signature from jpackage.

Both runs use a throw-away home directory, so the settings and the index of the user are not touched.

## Requirements
- The cache is bound to the exact runtime inside the app image and to the platform. Create it on the operating system the
  application is built for. A cache from another build is ignored by the JVM.
- The recording run shows the main window. A build machine needs a display. On Linux run
  `xvfb-run ./gradlew jpackage -PaotCache`.
- The path of the temporary directory has to be short (a unix domain socket path is limited to about 104 characters on
  macOS). The default temp directories are fine.

## If something fails
The build never fails because of the cache. A warning `AOT cache: could not be created...` is logged and the app image
stays without cache and without the entry in the launcher configuration. The recording run counts as failed if the
application did not log `Started DocumentFinder`.

## Check the result
```shell
ls build/jpackage/*.app/Contents/app/document-finder.aot        # macOS
grep AOTCache build/jpackage/*.app/Contents/app/*.cfg
codesign --verify --deep --strict build/jpackage/*.app           # macOS
```
To make sure a start really uses the cache, start the launcher with `JAVA_TOOL_OPTIONS=-XX:AOTMode=on`. The JVM then
aborts if it cannot use the cache. `-XX:AOTMode=off` starts without it.

## Known limits
- The cache is created with `-XX:-AOTClassLinking`. With class linking enabled (the default of the JDK) writing the cache
  fails for this application with `EncodedKeySpec$$Lambda ... must be stateless`.
- The training run only starts the application and shows the window. Classes that are loaded later (search, indexing, the
  options window) are not part of the cache.
- If the JVM cannot read the cache it still starts the application, but prints `[error][aot]` lines on the console.
- Only macOS arm64 was tested. Windows and Linux need the same steps but were not tried.
