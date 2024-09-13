### About

smali/baksmali is an assembler/disassembler for the dex format used by dalvik, Android's Java VM implementation. The syntax is loosely based on Jasmin's/dedexer's syntax, and supports the full functionality of the dex format (annotations, debug info, line info, etc.)

**NOTE**: This is a fork of https://github.com/JesusFreke/smali for patches needed by Google as the original repository is currently not maintained. After forking the namespace was changed from `org.jf` to `com.android.tools.smali`. The artifacts are released on [Google Maven](https://maven.google.com) under the following coordinates:

* [`com.android.tools.smali:smali:<version>`](https://maven.google.com/web/index.html?q=smali#com.android.tools.smali:smali)
* [`com.android.tools.smali:smali-dexlib2:<version>`](https://maven.google.com/web/index.html?q=smali-dexlib2#com.android.tools.smali:smali-dexlib2)
* [`com.android.tools.smali:smali-baksmali:<version>`](https://maven.google.com/web/index.html?q=smali-baksmali#com.android.tools.smali:smali)
* [`com.android.tools.smali:smali-util:<version>`](https://maven.google.com/web/index.html?q=smali-util#com.android.tools.smali:smali-util)

After the fork the first version released was 3.0.0, which was version 2.5.2 from the original repo with a few patches and the namespace change.

#### Support
- [github Issue tracker](https://github.com/google/smali/issues) - For any bugs/issues/feature requests

#### Some useful links for getting started with smali

- [Official dex bytecode reference](https://source.android.com/devices/tech/dalvik/dalvik-bytecode.html)
- [Registers wiki page](https://github.com/JesusFreke/smali/wiki/Registers)
- [Types, Methods and Fields wiki page](https://github.com/JesusFreke/smali/wiki/TypesMethodsAndFields)
- [Official dex format reference](https://source.android.com/devices/tech/dalvik/dex-format.html)

### Building and testing

sAll building and testing should be done using a version of OpenJDK 11. Newer OpenJDK versions are currently not supported due to issues with some of the tools used in the build process.

#### Building
```
./gradlew assemble
```
#### Command Line Version

To run the `smali` and `baksmali` tools from the command line build the fat
jars. The fat jars will be named with the current version followed by the first
8 characters of the current git hash followed by an optional `-dirty` if the
repository was dirty when building and ending in  -fat . The fat jar can be
invoked with `java -jar`.
```
./gradlew smali:fatJar
java -jar smali/build/libs/smali-x.y.z-aaaaaaaa-dirty-fat.jar
```

#### Testing

To execute all tests run
```
./gradlew test
```

#### Testing Maven Release
Push a release version to your local maven repository (add
`-Dmaven.repo.local=<dir>` to override the default local maven repository
location)
```
./gradlew release publishToMavenLocal
```

### Releasing

Building release versions and releasing to [Google Maven](https://maven.google.com) use Google infrastructure and support scripts maintained as part of the [R8](https://r8.googlesource.com/r8/) repository. The tasks below can only be performed by Google employees.

#### Prepare and build a release version
To prepare a release update `build.gradle` with the next release version and commit that.
Then create a tag for that commit with the version.
```
git tag <version> <commit>
git push origin <version>
```
Release versions can then be built by the Google R8 team using:
```
tools/trigger.py --smali=<version> --release
```
in the R8 repository.

The status of the build on the bot is at https://ci.chromium.org/p/r8/builders/ci/smali.

#### Releasing to Google Maven

When a release version has been built on the bot, it can be released to [Google Maven](https://maven.google.com) by running 
```
tools/release_smali.py --version=<version>
```
in the R8 repository. This kick off an internal Google approval process to finalize the release.
