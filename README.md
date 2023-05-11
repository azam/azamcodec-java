# azamcodec-java

[![MIT licensed](https://img.shields.io/badge/license-mit-blue.svg)](https://raw.githubusercontent.com/azam/azamcodec-java/master/license)
[![Maven Central](https://img.shields.io/maven-central/v/io.azam.azamcodec/azamcodec)](https://central.sonatype.com/artifact/io.azam.azamcodec/azamcodec)
[![javadoc](https://javadoc.io/badge2/io.azam.azamcodec/azamcodec/javadoc.svg)](https://javadoc.io/doc/io.azam.azamcodec/azamcodec)
[![Build Status](https://github.com/azam/azamcodec-java/actions/workflows/build.yml/badge.svg)](https://github.com/azam/azamcodec-java/actions/workflows/build.yml)

An encoder and decoder implementation in Java for [Azam Codec](https://github.com/azam/azamcodec), a lexicographically sortable multi-section base16 encoding of byte array. Zero external dependencies.

## License

> MIT License Copyright (c) 2023 Azamshul Azizy

## Usage

This library is published to [Maven Central](https://central.sonatype.com/artifact/io.azam.azamcodec/azamcodec); you can add it as Maven/Gradle/etc dependency as needed. Refer [Maven Central](https://central.sonatype.com/artifact/io.azam.azamcodec/azamcodec) for latest published version and snippets on adding dependency to your project.

### Decoding

```java
// Decode to ints
int[] ints = AzamCodec.azamDecodeAllInts("");

// Decode to big endian bytes
byte[] bytes = AzamCodec.azamDecodeAllBytes("");
```

### Encoding

```java
// Encode from ints
String encodedInts = AzamCodec.azamEncodeInts(new int[]{});

// Encode from bytes
String encodedBytes = AzamCodec.azamEncodeBytes(new byte[][]{});
```

## Development

Standard Java development method applies.
Please run the following before sending a PR:

* You can format sources to match style with ```mvn formatter:format xml-format:xml-format```
  * You can also use VS Code extension
* Make sure tests are passing and source is properly formatted by running ```mvn verify```
