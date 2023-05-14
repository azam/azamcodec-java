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
int[] ints = AzamCodec.azamDecodeInts("xytxvyyfh5wgg1"); // -559038737, 21, 49153

// Decode to big endian bytes
byte[][] bytes = AzamCodec.azamDecodeBytes("xytxvyyfh5wgg1"); // 0xdeadbeef, 0x15, 0xc001
```

### Encoding

```java
// Encode from ints
String encodedInts = AzamCodec.azamEncodeInts(new int[]{ -559038737, 21, 49153 }); // "xytxvyyfh5wgg1"

// Encode from bytes
String encodedBytes = AzamCodec.azamEncodeBytes(new byte[][]{ //
    new byte[] { 0xde, 0xad, 0xbe, 0xef }, //
    new byte[] { 0x15 }, //
    new byte[] { 0xc0, 0x01 } //
}); // "xytxvyyfh5wgg1"
```

### Practical example

Azam Codec is designed to be a sortable identifier representation, so using it to represent multi sectioned identifier is the best example.

```java
public class RecordId {
  int tenantId;
  int objectId;
  int recordId;

  public RecordId(String id) throws ParseException {
    int[] sections = AzamCodec.azamDecodeInts(id);
    this.tenantId = sections[0];
    this.objectId = sections[1];
    this.recordId = sections[2];
  }

  public String toString() {
    return AzamCodec.azamEncodeInts(new int[] { this.tenantId, this.objectId, this.recordId });
  }
}
```

## Development

Standard Java development method applies.
Please make sure that code is correctly formatted and tests are passing before sending a PR.

### Format source code
```sh
mvn formatter:format xml-format:xml-format
```

### Test
```sh
mvn verify
```

### Benchmark

```sh
mvn -P benchmark package
java -jar target/benchmark.jar AzamCodecBench
```
