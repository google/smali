### About

smali/baksmali is an assembler/disassembler for the dex format used by dalvik, Android's Java VM implementation. The syntax is loosely based on Jasmin's/dedexer's syntax, and supports the full functionality of the dex format (annotations, debug info, line info, etc.)

**NOTE**: This is a fork of https://github.com/JesusFreke/smali for patches needed by Google as the original repository is currently not maintained. After forking the namespace was changed from `org.jf` to `com.android.tools.smali`. The artifacts are released on [Google Maven](https://maven.google.com) under the following coordinates:
* [com.android.tools.smali:smali:<version>](https://maven.google.com/web/index.html?q=smali#com.android.tools.smali:smali)
* [com.android.tools.smali:smali-dexlib2:<version>](https://maven.google.com/web/index.html?q=smali-dexlib2#com.android.tools.smali:smali-dexlib2)
* [com.android.tools.smali:smali-baksmali:<version>](https://maven.google.com/web/index.html?q=smali-baksmali#com.android.tools.smali:smali)
* [com.android.tools.smali:smali-util:<version>](https://maven.google.com/web/index.html?q=smali-util#com.android.tools.smali:smali-util)
After the fork the first version released was 3.0.0, which was version 2.5.2 from the original repo with a few patches and the namespace change.

#### Support
- [github Issue tracker](https://github.com/google/smali/issues) - For any bugs/issues/feature requests

#### Some useful links for getting started with smali

- [Official dex bytecode reference](https://source.android.com/devices/tech/dalvik/dalvik-bytecode.html)
- [Registers wiki page](https://github.com/JesusFreke/smali/wiki/Registers)
- [Types, Methods and Fields wiki page](https://github.com/JesusFreke/smali/wiki/TypesMethodsAndFields)
- [Official dex format reference](https://source.android.com/devices/tech/dalvik/dex-format.html)

### Building
```
./gradlew assemble
```
### Command Line Version

To run the `smali` and `baksmali` tools from the command line build the fat
jars. The fat jars will be named with the current version followed by the first
8 characters of the current git hash followed by an optional `-dirty` if the
repository was dirty when building and ending in  -fat . The fat jar can be
invoked with `java -jar`.
```
./gradlew smali:fatJar
java -jar smali/build/libs/smali-x.y.z-aaaaaaaa-dirty-fat.jar
```

### Testing

To execute all tests run
```
./gradlew test
```

### Testing Maven Release
Push a release version to your local maven repository (add
`-Dmaven.repo.local=<dir>` to override the default local maven repository
location)
```
./gradlew release publishToMavenLocal
```
