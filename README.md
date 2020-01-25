# KBool

[![Build Status](https://travis-ci.org/xeroli/kbool.svg?branch=master)](https://travis-ci.org/xeroli/kbool) [![Apache License](https://img.shields.io/badge/license-Apache%20License%202.0-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0)
## Summary

**KBool** is a simple kotlin library providing a transparent boolean algebra.

### Code example
```
val sunIsShining = true.asBool()
println(sunIsShining.isTrue()) // -> true

val isRaining = false.asBool().named("isRaining")
println(!isRaining.booleanValue()) // -> true

val haveUmbrella = true.asBool("haveUmbrella")

val walkingInTheWood = sunIsShining.named("sunIsShining") 
        and (!isRaining or haveUmbrella)

println(walkingInTheWood.isTrue()) // -> true, but why?
println(walkingInTheWood.getCause()) // -> sunIsShining - true, isRaining - false
                                     //    so an umbrella doesn't change actually a thing ;-)
```

## Latest Stable Release

#### Download

[ ![Download](https://api.bintray.com/packages/xeroli/maven/kbool/images/download.svg) ](https://bintray.com/xeroli/maven/kbool/_latestVersion)

#### Maven
```xml
...
<repositories>
  <repository>
    <id>jcenter</id>
    <url>https://jcenter.bintray.com/</url>
  </repository>
</repositories>

<dependencies>
  <dependency>
    <groupId>de.xeroli.kbool</groupId>
    <artifactId>kbool</artifactId>
    <version>0.2.0</version>
  </dependency>
</dependencies>
...
```

#### Gradle
```groovy
repositories {
  jcenter()
}

dependencies {
  compile('de.xeroli.kbool:kbool:0.2.0')
}
```

## Snapshot

[![Build Status](https://travis-ci.com/xeroli/kbool.svg?branch=snapshot)](https://travis-ci.com/xeroli/kbool)

You can access the latest snapshot by adding "-SNAPSHOT" to the version number and
adding the repository `https://oss.jfrog.org/artifactory/oss-snapshot-local`
to your build.

You can also reference a specific snapshot like `0.2.0-20200125.081709-1`. 
Here's the [list of snapshot versions](https://oss.jfrog.org/webapp/#/artifacts/browse/tree/General/oss-snapshot-local/de/xeroli/kbool/kbool).

#### Maven
```xml
...
<repositories>
  <repository>
    <id>oss-snapshot-local</id>
    <url>https://oss.jfrog.org/webapp/#/artifacts/browse/tree/General/oss-snapshot-local/de/xeroli/kbool/kbool</url>
  </repository>
</repositories>

<dependencies>
  <dependency>
    <groupId>de.xeroli.kbool</groupId>
    <artifactId>kbool</artifactId>
    <version>0.3.0-SNAPSHOT</version>
  </dependency>
</dependencies>
...
```

#### Gradle
```groovy
repositories {
  maven { url 'https://oss.jfrog.org/artifactory/oss-snapshot-local' }
}

dependencies {
  compile('de.xeroli.kbool:kbool:0.3.0-SNAPSHOT')
}
```




