package io.azam.azamcodec;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;

/**
 * Azam Codec encoder/decoder.
 * 
 * @author azam
 * @since 0.0.1
 */
public class AzamCodec {
  final static int[] LOWER_ALPHABETS = new int[] { //
      '0', '1', '2', '3', '4', '5', '6', '7', //
      '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' //
  };
  final static int[] HIGHER_ALPHABETS = new int[] { //
      'g', 'h', 'j', 'k', 'm', 'n', 'p', 'q', //
      'r', 's', 't', 'v', 'w', 'x', 'y', 'z' //
  };

  /**
   * Consume bytes from `input`, generates Azam Codec encoded string as bytes, and writes to
   * `output`
   * 
   * @param output Output stream
   * @param input Input stream
   * @throws IOException
   */
  public static void azamEncodeStream(OutputStream output, InputStream input) throws IOException {
    int buf = -1;
    int prevBuf = -1;
    int count = 0;

    /// 0: None, 1: Previous is high, 2: Previous is low
    int leadNybbleStatus = 0;

    buf = input.read();
    if (buf == -1)
      return;
    if (buf >> 4 > 0) {
      leadNybbleStatus = 1;
    } else if ((((byte) buf) & 0xf) > 0) {
      leadNybbleStatus = 2;
    } else {
      leadNybbleStatus = 0;
    }
    count++;
    prevBuf = buf;

    for (;;) {
      buf = input.read();
      count++;

      // EOF
      if (buf == -1)
        break;

      if (leadNybbleStatus == 0) {
        // Not flushing previous byte
        // Update lead nybble status
        if (buf >> 4 > 0) {
          leadNybbleStatus = 1;
        } else if (((byte) buf & 0xf) > 0) {
          leadNybbleStatus = 2;
        } else {
          leadNybbleStatus = 0;
        }
      } else if (leadNybbleStatus == 1) {
        // Flush previous byte as highs
        int highNybble = prevBuf >> 4;
        int lowNybble = (byte) prevBuf & 0x0f;
        output.write(HIGHER_ALPHABETS[highNybble]);
        output.write(HIGHER_ALPHABETS[lowNybble]);
      } else if (leadNybbleStatus == 2) {
        // Only flush previous byte's low nybble as high
        int lowNybble = (byte) prevBuf & 0x0f;
        output.write(HIGHER_ALPHABETS[lowNybble]);
        leadNybbleStatus = 1;
      }

      prevBuf = buf;
    }

    // Flush last byte
    if (leadNybbleStatus == 0) {
      if (count > 0) {
        // Previous byte is 0x00
        // Flush as low nybble
        output.write(LOWER_ALPHABETS[0]);
      }
    } else if (leadNybbleStatus == 1) {
      // Flush previous byte's high nybble as high
      // Flush previous byte's low nybble as low
      int highNybble = prevBuf >> 4;
      int lowNybble = ((byte) prevBuf) & 0x0f;
      output.write(HIGHER_ALPHABETS[highNybble]);
      output.write(LOWER_ALPHABETS[lowNybble]);
    } else if (leadNybbleStatus == 2) {
      // Flush previous byte's low nybble as low
      int lowNybble = (byte) prevBuf & 0x0f;
      output.write(LOWER_ALPHABETS[lowNybble]);
    }
  }

  /**
   * Consume bytes from multiple streams `inputs` sequentially, generates Azam Codec encoded string
   * as bytes, and writes to `output`
   * 
   * @param output Output stream
   * @param inputs Input streams
   * @throws IOException
   */
  public static void azamEncodeStreams(OutputStream output, InputStream... inputs)
      throws IOException {
    if (inputs == null)
      throw new IllegalArgumentException("Argument is null");
    for (InputStream input : inputs) {
      if (input == null)
        throw new IllegalArgumentException("Arguments contains null value");
      azamEncodeStream(output, input);
    }
  }

  /**
   * For each byte array `values`, generate Azam Codec encoded string section, concatenate all
   * sections and returns the string.
   * 
   * @param values Input byte arrays
   * @return Azam Codec encoded string
   */
  public static String azamEncodeBytes(byte[]... values) {
    if (values == null)
      throw new IllegalArgumentException("Value is null");
    try (ByteArrayOutputStream output = new ByteArrayOutputStream(values.length)) {
      for (byte[] value : values) {
        if (value == null)
          throw new IllegalArgumentException("Value contains null value");
        try (ByteArrayInputStream input = new ByteArrayInputStream(value)) {
          azamEncodeStream(output, input);
        }
      }
      // All Azam Codec characters are ASCII so this should do fine
      return new String(output.toByteArray(), StandardCharsets.US_ASCII);
    } catch (IOException io) {
      // We should not get here because we are only using byte array streams,
      // unless there are severe memory IO errors
      throw new IllegalArgumentException("Unexpected IOException", io);
    }
  }

  /**
   * For each {@link java.lang.Number} array of `values`, generate Azam Codec encoded string section
   * based on the number's byte representation in Big-Endian, concatenate all sections and returns
   * the string.
   * 
   * Supported instances are:
   * 
   * <ul>
   * <li>{@link java.lang.Integer}</li>
   * <li>{@link java.lang.Long}</li>
   * <li>{@link java.math.BigInteger}</li>
   * </ul>
   * 
   * @param values Input numbers
   * @return Azam Codec encoded string
   */
  public static String azamEncodeNumbers(Number... values) {
    if (values == null)
      throw new IllegalArgumentException("Value is null");
    try (ByteArrayOutputStream output = new ByteArrayOutputStream(values.length)) {
      for (Number value : values) {
        if (value == null)
          throw new IllegalArgumentException("Value contains null");
        byte[] bytes;
        if (value instanceof Integer) {
          int data = value.intValue();
          bytes = new byte[] { //
              (byte) ((data >> 24) & 0xff), //
              (byte) ((data >> 16) & 0xff), //
              (byte) ((data >> 8) & 0xff), //
              (byte) (data & 0xff), //
          };
        } else if (value instanceof Long) {
          long data = value.longValue();
          bytes = new byte[] { //
              (byte) ((data >> 56) & 0xff), //
              (byte) ((data >> 48) & 0xff), //
              (byte) ((data >> 40) & 0xff), //
              (byte) ((data >> 32) & 0xff), //
              (byte) ((data >> 24) & 0xff), //
              (byte) ((data >> 16) & 0xff), //
              (byte) ((data >> 8) & 0xff), //
              (byte) (data & 0xff), //
          };
        } else if (value instanceof BigInteger) {
          BigInteger data = (BigInteger) value;
          bytes = data.toByteArray();
        } else {
          throw new IllegalArgumentException("Value is not a supported Number");
        }
        try (ByteArrayInputStream input = new ByteArrayInputStream(bytes)) {
          azamEncodeStream(output, input);
        }
      }
      return new String(output.toByteArray(), StandardCharsets.US_ASCII);
    } catch (IOException io) {
      // We should not get here because we are only using byte array streams,
      // unless there are severe memory IO errors
      throw new IllegalArgumentException("Unexpected IOException", io);
    }
  }

  /**
   * For each int array of `values`, generate Azam Codec encoded string section based on the
   * number's byte representation in Big-Endian, concatenate all sections and returns the string.
   * 
   * @param values Input numbers
   * @return Azam Codec encoded string
   */
  public static String azamEncodeInts(int... values) {
    if (values == null)
      throw new IllegalArgumentException("Values are null");
    try (ByteArrayOutputStream output = new ByteArrayOutputStream(values.length)) {
      for (int value : values) {
        byte[] bytes = new byte[] { //
            (byte) ((value >> 24) & 0xff), //
            (byte) ((value >> 16) & 0xff), //
            (byte) ((value >> 8) & 0xff), //
            (byte) (value & 0xff), //
        };
        try (ByteArrayInputStream input = new ByteArrayInputStream(bytes)) {
          azamEncodeStream(output, input);
        }
      }
      return new String(output.toByteArray(), StandardCharsets.US_ASCII);
    } catch (IOException io) {
      // We should not get here because we are only using byte array streams,
      // unless there are severe memory IO errors
      throw new IllegalArgumentException("Unexpected IOException", io);
    }
  }

  /**
   * For each long array of `values`, generate Azam Codec encoded string section based on the
   * number's byte representation in Big-Endian, concatenate all sections and returns the string.
   * 
   * @param values Input numbers
   * @return Azam Codec encoded string
   */
  public static String azamEncodeLongs(long... values) {
    if (values == null)
      throw new IllegalArgumentException("Values are null");
    try (ByteArrayOutputStream output = new ByteArrayOutputStream(values.length)) {
      for (long value : values) {
        byte[] bytes = new byte[] { //
            (byte) ((value >> 56) & 0xff), //
            (byte) ((value >> 48) & 0xff), //
            (byte) ((value >> 40) & 0xff), //
            (byte) ((value >> 32) & 0xff), //
            (byte) ((value >> 24) & 0xff), //
            (byte) ((value >> 16) & 0xff), //
            (byte) ((value >> 8) & 0xff), //
            (byte) (value & 0xff), //
        };
        try (ByteArrayInputStream input = new ByteArrayInputStream(bytes)) {
          azamEncodeStream(output, input);
        }
      }
      return new String(output.toByteArray(), StandardCharsets.US_ASCII);
    } catch (IOException io) {
      // We should not get here because we are only using byte array streams,
      // unless there are severe memory IO errors
      throw new IllegalArgumentException("Unexpected IOException", io);
    }
  }

  final static byte getNybbleValue(final int symbol) {
    switch (symbol) {
      // Lower nybble
      case '0':
      case 'o':
      case 'O':
        return (byte) 0x00;
      case '1':
      case 'i':
      case 'I':
      case 'l':
      case 'L':
        return (byte) 0x01;
      case '2':
        return (byte) 0x02;
      case '3':
        return (byte) 0x03;
      case '4':
        return (byte) 0x04;
      case '5':
        return (byte) 0x05;
      case '6':
        return (byte) 0x06;
      case '7':
        return (byte) 0x07;
      case '8':
        return (byte) 0x08;
      case '9':
        return (byte) 0x09;
      case 'a':
      case 'A':
        return (byte) 0x0a;
      case 'b':
      case 'B':
        return (byte) 0x0b;
      case 'c':
      case 'C':
        return (byte) 0x0c;
      case 'd':
      case 'D':
        return (byte) 0x0d;
      case 'e':
      case 'E':
        return (byte) 0x0e;
      case 'f':
      case 'F':
        return (byte) 0x0f;
      // Higher nybble
      case 'g':
      case 'G':
        return (byte) 0x10;
      case 'h':
      case 'H':
        return (byte) 0x11;
      case 'j':
      case 'J':
        return (byte) 0x12;
      case 'k':
      case 'K':
        return (byte) 0x13;
      case 'm':
      case 'M':
        return (byte) 0x14;
      case 'n':
      case 'N':
        return (byte) 0x15;
      case 'p':
      case 'P':
        return (byte) 0x16;
      case 'q':
      case 'Q':
        return (byte) 0x17;
      case 'r':
      case 'R':
        return (byte) 0x18;
      case 's':
      case 'S':
        return (byte) 0x19;
      case 't':
      case 'T':
        return (byte) 0x1a;
      case 'v':
      case 'V':
        return (byte) 0x1b;
      case 'w':
      case 'W':
        return (byte) 0x1c;
      case 'x':
      case 'X':
        return (byte) 0x1d;
      case 'y':
      case 'Y':
        return (byte) 0x1e;
      case 'z':
      case 'Z':
        return (byte) 0x1f;
      default:
        return (byte) 0xff;
    }
  }

  /**
   * Consume a single section of an Azam Codec encoded stream from `input` and write decoded bytes
   * to `output`.
   *
   * @param input Input stream
   * @param output Output stream
   * @throws EOFException On end of a stream
   * @throws IOException On unexpected IO exceptions
   * @throws ParseException On invalid Azam Codec characters and/or character orders
   */
  public static void azamDecodeStreamSection(InputStream input, OutputStream output)
      throws EOFException, IOException, ParseException {
    // Bitshift operators recasts to integer, so we have to
    // mask byte variables with 0xff before doing integer operations.

    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    byte prevNybble = 0x00; /// Must always be 0x0..0xf . No need to mask with 0xff
    boolean isOdd = false;
    boolean leadNybbleChecked = false;
    int count = 0;

    for (;;) {
      int buf = input.read();
      // EOF
      // Unreachable for valid strings (ending with lower nybble aplhabets)
      if (buf == -1) {
        if (count == 0) {
          // Empty stream
          throw new EOFException();
        } else {
          throw new ParseException("Invalid encoded value (does not end with lower nybble char)",
              count);
        }
      }
      count++;

      byte value = getNybbleValue(buf);

      // Invalid
      if (value == (byte) 0xff)
        throw new ParseException("Invalid encoded value (unknown char)", count - 1);

      // Flip oddness
      isOdd = !isOdd;

      if (!leadNybbleChecked) {
        // If the first byte starts with a high nibble 0 (g or G), return error as
        // invalid data
        if (value == (byte) 0x10)
          throw new ParseException("Invalid encoded value ('g' cannot be a leading char)",
              count - 1);
        leadNybbleChecked = true;
      }

      // Take previous nybble, shift left 4 and bit or current nybble
      if (!isOdd) {
        bytes.write((byte) ((prevNybble << 4) | (value & 0x0f)));
      }

      // Remember current nybble for next iteration
      prevNybble = (byte) (value & 0x0f);

      // If current nybble is a low nybble, this is the last one, so exit loop
      if ((value & 0xff) >> 4 == 0x00)
        break;
    }

    // If nybble count is odd, then there is one unwritten nybble.
    // Add the unwritten nybble, and shift whole byte array 4 bits to the right.
    if (isOdd && count > 0) {
      // Flush last nybble. This will be shifted later.
      bytes.write((byte) (prevNybble << 4));

      byte[] arr = bytes.toByteArray();

      // Right shift byte array for 4 bits, in place
      byte carry = (byte) 0x00; /// Must always be 0x0..0xf
      for (int i = 0; i < arr.length; i++) {
        byte b = arr[i];
        arr[i] = (byte) ((b & 0xff) >> 4 | (carry << 4));
        carry = (byte) (b & 0x0f);
      }

      // Flush ByteBuffer to OutputStream
      output.write(arr);
      return;
    }

    // Flush ByteBuffer to OutputStream
    output.write(bytes.toByteArray());
    return;
  }

  /**
   * Decode all sections of an Azam Codec encoded string `value` as arrays of byte array.
   * 
   * @param value Azam Codec encoded string
   * @return Decoded value as array of byte array
   * @throws ParseException On invalid Azam Codec characters and/or character orders
   */
  public static byte[][] azamDecodeBytes(String value) throws ParseException {
    if (value == null)
      throw new IllegalArgumentException("Argument is null");
    try (InputStream input = new ByteArrayInputStream(value.getBytes())) {
      byte[][] values = new byte[0][];
      for (int i = 0;; i++) {
        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
          azamDecodeStreamSection(input, output);

          // Extend return array
          byte[][] extended = new byte[i + 1][];
          if (values.length > 0) {
            System.arraycopy(values, 0, extended, 0, values.length);
          }
          extended[i] = output.toByteArray();
          values = extended;
        } catch (EOFException eof) {
          // azamDecodeStream throws EOFException only on empty streams,
          // so when we receive this, it should be the last one to ignore
          break;
        }
      }
      return values;
    } catch (IOException io) {
      // We should not get here because we are only using byte array streams,
      // unless there are severe memory IO errors
      throw new ParseException("Unexpected IO Exception: " + io.getMessage(), -1);
    }
  }

  /**
   * Decode all sections of an Azam Codec encoded string `value` as int array.
   * 
   * @param value Azam Codec encoded string
   * @return Decoded value as int array
   * @throws ParseException On invalid Azam Codec characters and/or character orders
   */
  public static int[] azamDecodeInts(String value) throws ParseException {
    if (value == null)
      throw new IllegalArgumentException("Argument is null");
    try (InputStream input = new ByteArrayInputStream(value.getBytes())) {
      int[] values = new int[0];
      for (int i = 0;; i++) {
        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
          azamDecodeStreamSection(input, output);
          byte[] bytes = output.toByteArray();

          // BigEndian byte array to int
          if (bytes == null || bytes.length == 0 || bytes.length > Integer.BYTES)
            throw new ParseException("Encoded value is empty or too long to convert to int", -1);
          int decoded = 0;
          for (int j = 0; j < bytes.length; j++) {
            decoded = decoded << 8 | (bytes[j] & 0xff);
          }

          // Extend return array
          int[] extended = new int[i + 1];
          if (values.length > 0) {
            System.arraycopy(values, 0, extended, 0, values.length);
          }
          extended[i] = decoded;
          values = extended;
        } catch (EOFException eof) {
          // azamDecodeStream throws EOFException only on empty streams,
          // so when we receive this, it should be the last one to ignore
          break;
        }
      }
      return values;
    } catch (IOException io) {
      // We should not get here because we are only using byte array streams,
      // unless there are severe memory IO errors
      throw new ParseException("Unexpected IO Exception: " + io.getMessage(), -1);
    }
  }

  /**
   * Decode all sections of an Azam Codec encoded string `value` as long array.
   * 
   * @param value Azam Codec encoded string
   * @return Decoded value as long array
   * @throws ParseException On invalid Azam Codec characters and/or character orders
   */
  public static long[] azamDecodeLongs(String value) throws ParseException {
    if (value == null)
      throw new IllegalArgumentException("Argument is null");
    try (InputStream input = new ByteArrayInputStream(value.getBytes())) {
      long[] values = new long[0];
      for (int i = 0;; i++) {
        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
          azamDecodeStreamSection(input, output);
          byte[] bytes = output.toByteArray();

          // BigEndian byte array to int
          if (bytes == null || bytes.length == 0 || bytes.length > Long.BYTES)
            throw new ParseException("Encoded value is empty or too long to convert to int", -1);
          long decoded = 0;
          for (int j = 0; j < bytes.length; j++) {
            decoded = decoded << 8 | (bytes[j] & 0xff);
          }

          // Extend return array
          long[] extended = new long[i + 1];
          if (values.length > 0) {
            System.arraycopy(values, 0, extended, 0, values.length);
          }
          extended[i] = decoded;
          values = extended;
        } catch (EOFException eof) {
          // azamDecodeStream throws EOFException only on empty streams,
          // so when we receive this, it should be the last one to ignore
          break;
        }
      }
      return values;
    } catch (IOException io) {
      // We should not get here because we are only using byte array streams,
      // unless there are severe memory IO errors
      throw new ParseException("Unexpected IO Exception: " + io.getMessage(), -1);
    }
  }
}
