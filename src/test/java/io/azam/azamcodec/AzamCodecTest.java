package io.azam.azamcodec;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.function.ThrowingRunnable;

import static io.azam.azamcodec.AzamCodec.*;

/**
 * Test class for {@link io.azam.azamcodec.AzamCodecs}
 *
 * @author azam
 * @since 0.0.1
 */
public class AzamCodecTest {
  static class Sample {
    String encoded = null;
    byte[][] bytes = null;
    long[] longs = null;

    public Sample() {}

    public Sample(String encoded, byte[][] bytes, long[] longs) {
      this.encoded = encoded;
      this.bytes = bytes;
      this.longs = longs;
    }

    public int[] ints() {
      // This is equivalent of the following (since we try to keep source compatible with 1.7)
      // return Arrays.stream(this.longs).mapToInt(Math::toIntExact).toArray();
      int[] ints = new int[this.longs.length];
      for (int i = 0; i < ints.length; i++) {
        // Converts long to int on byte level not value
        // i.e. 0xffffffffL will convert to -1 , not 4294967295 (overflow)
        // NOTE: to do a value conversion, use ints[i] = Math.toIntExact(this.longs[i]); instead
        ints[i] = (int) this.longs[i] & 0xffffffff;
      }
      return ints;
    }

    public Number[] nums() {
      // This is equivalent of the following (since we try to keep source compatible with 1.7)
      // return Arrays.stream(this.longs).mapToInt(Math::toIntExact).toArray();
      Number[] nums = new Number[this.longs.length];
      for (int i = 0; i < nums.length; i++) {
        // Converts long to int on byte level not value
        // i.e. 0xffffffffL will convert to -1 , not 4294967295 (overflow)
        // NOTE: to do a value conversion, use ints[i] = Math.toIntExact(this.longs[i]); instead
        nums[i] = Long.valueOf(this.longs[i]);
      }
      return nums;
    }

    public int largestBytesLength() {
      int max = 0;
      for (int i = 0; i < this.bytes.length; i++) {
        if (this.bytes[i].length > max) {
          max = this.bytes[i].length;
        }
      }
      return max;
    }

    public Sample e(String encoded) {
      this.encoded = encoded;
      return this;
    }

    public Sample b(byte[]... bytes) {
      this.bytes = bytes;
      return this;
    }

    public Sample l(long... longs) {
      this.longs = longs;
      return this;
    }
  }

  static Sample S(String encoded) {
    return new Sample(encoded, null, null);
  }

  static List<Sample> SAMPLES = Arrays.asList(
      // Single sections
      S("0").b(b(0x00)).l(0x00), //
      S("1").b(b(0x01)).l(0x01), //
      S("f").b(b(0x0f)).l(0x0f), //
      S("h0").b(b(0x10)).l(0x10), //
      S("h1").b(b(0x11)).l(0x11), //
      S("hf").b(b(0x1f)).l(0x1f), //
      S("z0").b(b(0xf0)).l(0xf0), //
      S("z1").b(b(0xf1)).l(0xf1), //
      S("zf").b(b(0xff)).l(0xffL), //
      S("hg0").b(b(0x01, 0x00)).l(0x0100L), //
      S("hg1").b(b(0x01, 0x01)).l(0x0101L), //
      S("hgf").b(b(0x01, 0x0f)).l(0x010fL), //
      S("hh0").b(b(0x01, 0x10)).l(0x0110L), //
      S("zg0").b(b(0x0f, 0x00)).l(0x0f00L), //
      S("zz0").b(b(0x0f, 0xf0)).l(0x0ff0L), //
      S("zz1").b(b(0x0f, 0xf1)).l(0x0ff1L), //
      S("zzf").b(b(0x0f, 0xff)).l(0x0fffL), //
      S("hgg0").b(b(0x10, 0x00)).l(0x1000L), //
      S("hgh0").b(b(0x10, 0x10)).l(0x1010L), //
      S("zgg0").b(b(0xf0, 0x00)).l(0xf000L), //
      S("zzz0").b(b(0xff, 0xf0)).l(0xfff0L), //
      S("zzz1").b(b(0xff, 0xf1)).l(0xfff1L), //
      S("zzzf").b(b(0xff, 0xff)).l(0xffffL), //
      S("hggg0").b(b(0x01, 0x00, 0x00)).l(0x010000L), //
      S("hggg1").b(b(0x01, 0x00, 0x01)).l(0x010001L), //
      S("hghg1").b(b(0x01, 0x01, 0x01)).l(0x010101L), //
      S("hggggg0").b(b(0x01, 0x00, 0x00, 0x00)).l(0x01000000L), //
      S("hggggg1").b(b(0x01, 0x00, 0x00, 0x01)).l(0x01000001L), //
      S("hgggggf").b(b(0x01, 0x00, 0x00, 0x0f)).l(0x0100000fL), //
      S("zzzzzzf").b(b(0x0f, 0xff, 0xff, 0xff)).l(0x0fffffffL), //
      S("hgggggg0").b(b(0x10, 0x00, 0x00, 0x00)).l(0x10000000L), //
      S("hgggggg1").b(b(0x10, 0x00, 0x00, 0x01)).l(0x10000001L), //
      S("hggggggf").b(b(0x10, 0x00, 0x00, 0x0f)).l(0x1000000fL), //
      S("zzzzzzzf").b(b(0xff, 0xff, 0xff, 0xff)).l(0xffffffffL));

  static List<Sample> MULTI_SAMPLES = new ArrayList<Sample>() {
    {
      // 2 sections
      for (Sample s1 : SAMPLES) {
        for (Sample s2 : SAMPLES) {
          String encoded = s1.encoded + s2.encoded;
          byte[][] bytes = new byte[s1.bytes.length + s2.bytes.length][];
          for (int i = 0; i < s1.bytes.length; i++) {
            bytes[i] = new byte[s1.bytes[i].length];
            System.arraycopy(s1.bytes[i], 0, bytes[i], 0, s1.bytes[i].length);
          }
          for (int i = 0; i < s2.bytes.length; i++) {
            bytes[i + s1.bytes.length] = new byte[s2.bytes[i].length];
            System.arraycopy(s2.bytes[i], 0, bytes[i + s1.bytes.length], 0, s2.bytes[i].length);
          }
          long[] longs = new long[s1.longs.length + s2.longs.length];
          System.arraycopy(s1.longs, 0, longs, 0, s1.longs.length);
          System.arraycopy(s2.longs, 0, longs, s1.longs.length, s2.longs.length);
          add(S(encoded).b(bytes).l(longs));
        }
      }
      // 3 sections
      for (Sample s1 : SAMPLES) {
        for (Sample s2 : SAMPLES) {
          for (Sample s3 : SAMPLES) {
            String encoded = s1.encoded + s2.encoded + s3.encoded;
            byte[][] bytes = new byte[s1.bytes.length + s2.bytes.length + s3.bytes.length][];
            for (int i = 0; i < s1.bytes.length; i++) {
              bytes[i] = new byte[s1.bytes[i].length];
              System.arraycopy(s1.bytes[i], 0, bytes[i], 0, s1.bytes[i].length);
            }
            for (int i = 0; i < s2.bytes.length; i++) {
              bytes[i + s1.bytes.length] = new byte[s2.bytes[i].length];
              System.arraycopy(s2.bytes[i], 0, bytes[i + s1.bytes.length], 0, s2.bytes[i].length);
            }
            for (int i = 0; i < s3.bytes.length; i++) {
              bytes[i + s1.bytes.length + s2.bytes.length] = new byte[s3.bytes[i].length];
              System.arraycopy(s3.bytes[i], 0, bytes[i + s1.bytes.length + s2.bytes.length], 0,
                  s3.bytes[i].length);
            }
            long[] longs = new long[s1.longs.length + s2.longs.length + s3.longs.length];
            System.arraycopy(s1.longs, 0, longs, 0, s1.longs.length);
            System.arraycopy(s2.longs, 0, longs, s1.longs.length, s2.longs.length);
            System.arraycopy(s3.longs, 0, longs, s1.longs.length + s2.longs.length,
                s3.longs.length);
            add(S(encoded).b(bytes).l(longs));
          }
        }
      }
    }
  };

  final static byte[] b(int... values) {
    byte[] value = new byte[values.length];
    for (int i = 0; i < values.length; i++) {
      value[i] = (byte) (values[i] & 0xff);
    }
    return value;
  }

  final static byte[][] bb(byte[]... values) {
    byte[][] value = new byte[values.length][];
    for (int i = 0; i < values.length; i++) {
      value[i] = values[i];
    }
    return value;
  }

  @Test
  public void testAzamEncodeBytes() {
    for (Sample sample : SAMPLES) {
      Assert.assertEquals("azamEncodeBytes failed for value " + sample.encoded, sample.encoded,
          azamEncodeBytes(sample.bytes));
    }
    for (Sample sample : MULTI_SAMPLES) {
      Assert.assertEquals("azamEncodeBytes failed for value " + sample.encoded, sample.encoded,
          azamEncodeBytes(sample.bytes));
    }
  }

  @Test
  public void testAzamEncodeInts() {
    for (Sample sample : SAMPLES) {
      Assert.assertEquals("azamEncodeLongs failed for value " + sample.encoded, sample.encoded,
          azamEncodeInts(sample.ints()));
    }
    for (Sample sample : MULTI_SAMPLES) {
      Assert.assertEquals("azamEncodeLongs failed for value " + sample.encoded, sample.encoded,
          azamEncodeInts(sample.ints()));
    }
  }

  @Test
  public void testAzamEncodeLongs() {
    for (Sample sample : SAMPLES) {
      Assert.assertEquals("azamEncodeLongs failed for value " + sample.encoded, sample.encoded,
          azamEncodeLongs(sample.longs));
    }
    for (Sample sample : MULTI_SAMPLES) {
      Assert.assertEquals("azamEncodeLongs failed for value " + sample.encoded, sample.encoded,
          azamEncodeLongs(sample.longs));
    }
  }

  @Test
  public void testAzamEncode() {
    for (Sample sample : SAMPLES) {
      Assert.assertEquals("azamEncodeNumbers failed for value " + sample.encoded, sample.encoded,
          azamEncodeNumbers(sample.nums()));
    }
    for (Sample sample : MULTI_SAMPLES) {
      Assert.assertEquals("azamEncodeNumbers failed for value " + sample.encoded, sample.encoded,
          azamEncodeNumbers(sample.nums()));
    }
  }

  @Test
  public void testAzamDecodeAllBytes() throws ParseException {
    for (Sample sample : SAMPLES) {
      byte[][] actual = azamDecodeBytes(sample.encoded);
      for (int i = 0; i < sample.bytes.length; i++) {
        Assert.assertArrayEquals("azamDecodeBytes failed for " + sample.encoded, sample.bytes[i],
            actual[i]);
      }
    }
    for (Sample sample : MULTI_SAMPLES) {
      byte[][] actual = azamDecodeBytes(sample.encoded);
      for (int i = 0; i < sample.bytes.length; i++) {
        Assert.assertArrayEquals("azamDecodeBytes failed for " + sample.encoded, sample.bytes[i],
            actual[i]);
      }
    }
  }

  void assertAzamDecodeInts(String value, int... expected) {
    try {
      int[] actual = azamDecodeInts(value);
      Assert.assertArrayEquals("azamDecodeInts failed for " + value, expected, actual);
    } catch (ParseException e) {
      Assert.fail("azamDecodeInts throws ParseException");
    }
  }

  @Test
  public void testAzamDecodeInts() throws ParseException {
    for (Sample sample : SAMPLES) {
      if (sample.largestBytesLength() <= Integer.BYTES) {
        int[] actual = azamDecodeInts(sample.encoded);
        Assert.assertArrayEquals("azamDecodeInts failed for " + sample.encoded, sample.ints(),
            actual);
      }
    }
    for (Sample sample : MULTI_SAMPLES) {
      if (sample.largestBytesLength() <= Integer.BYTES) {
        int[] actual = azamDecodeInts(sample.encoded);
        Assert.assertArrayEquals("azamDecodeInts failed for " + sample.encoded, sample.ints(),
            actual);
      }
    }
  }

  @Test
  public void testAzamDecodeLongs() throws ParseException {
    for (Sample sample : SAMPLES) {
      if (sample.largestBytesLength() <= Long.BYTES) {
        long[] actual = azamDecodeLongs(sample.encoded);
        Assert.assertArrayEquals("azamDecodeLongs failed for " + sample.encoded, sample.longs,
            actual);
      }
    }
    for (Sample sample : MULTI_SAMPLES) {
      if (sample.largestBytesLength() <= Long.BYTES) {
        long[] actual = azamDecodeLongs(sample.encoded);
        Assert.assertArrayEquals("azamDecodeLongs failed for " + sample.encoded, sample.longs,
            actual);
      }
    }
  }

  @Test
  public void testAzamDecodeIntsParseException() {
    String[] invalids = new String[] {"h", "hh", "hhh", "_0", "gf", "hggggggg0"};
    for (String value : invalids) {
      ThrowingRunnable runnable = new ThrowingRunnable() {
        String value = null;

        public ThrowingRunnable setValue(String value) {
          this.value = value;
          return this;
        }

        @Override
        public void run() throws ParseException {
          int[] unexpected = azamDecodeInts(value);
        }
      }.setValue(value);
      Assert.assertThrows("azamDecodeInts expects ParseException for " + value,
          ParseException.class, runnable);
    }
  }

  @Test
  public void testAzamDecodeLongsParseException() {
    String[] invalids = new String[] {"h", "hh", "hhh", "_0", "gf", "hggggggggggggggg0"};
    for (String value : invalids) {
      ThrowingRunnable runnable = new ThrowingRunnable() {
        String value = null;

        public ThrowingRunnable setValue(String value) {
          this.value = value;
          return this;
        }

        @Override
        public void run() throws ParseException {
          long[] unexpected = azamDecodeLongs(value);
        }
      }.setValue(value);
      Assert.assertThrows("azamDecodeLongs expects ParseException for " + value,
          ParseException.class, runnable);
    }
  }

}
