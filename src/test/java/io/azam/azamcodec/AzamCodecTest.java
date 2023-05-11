package io.azam.azamcodec;

import java.io.IOException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.junit.function.ThrowingRunnable;

import static io.azam.azamcodec.AzamCodec.*;

/**
 * Test class for {@link io.azam.azamcodec.AzamDecode}
 *
 * @author azam
 * @since 0.0.1
 */
public class AzamCodecTest {
  final static Map<String, byte[][]> SAMPLES = new HashMap<String, byte[][]>() {
    {
      put("0", new byte[][] {bytes(0x00)});
      put("1", new byte[][] {bytes(0x01)});
      put("f", new byte[][] {bytes(0x0f)});
      put("h0", new byte[][] {bytes(0x10)});
      put("h1", new byte[][] {bytes(0x11)});
      put("hf", new byte[][] {bytes(0x1f)});
      put("z0", new byte[][] {bytes(0xf0)});
      put("z1", new byte[][] {bytes(0xf1)});
      put("zf", new byte[][] {bytes(0xff)});
      put("hg0", new byte[][] {bytes(0x01, 0x00)});
      put("hg1", new byte[][] {bytes(0x01, 0x01)});
      put("hgf", new byte[][] {bytes(0x01, 0x0f)});
      put("hh0", new byte[][] {bytes(0x01, 0x10)});
      put("zg0", new byte[][] {bytes(0x0f, 0x00)});
      put("zz0", new byte[][] {bytes(0x0f, 0xf0)});
      put("zz1", new byte[][] {bytes(0x0f, 0xf1)});
      put("zzf", new byte[][] {bytes(0x0f, 0xff)});
      put("hgg0", new byte[][] {bytes(0x10, 0x00)});
      put("zgg0", new byte[][] {bytes(0xf0, 0x00)});
      put("zzz1", new byte[][] {bytes(0xff, 0xf1)});
      put("zzzf", new byte[][] {bytes(0xff, 0xff)});
    }
  };

  final static byte[] bytes(int... values) {
    byte[] value = new byte[values.length];
    for (int i = 0; i < values.length; i++) {
      value[i] = (byte) (values[i] & 0xff);
    }
    return value;
  }

  final static void assertAzamEncodeBytes(String expected, byte[]... values) {
    try {
      String actual = azamEncodeBytes(values);
      Assert.assertEquals(expected, actual);
    } catch (IOException e) {
      Assert.fail("azamEncodeBytes throws IOException");
    }
  }

  @Test
  public void testAzamEncodeBytes() {
    for (Map.Entry<String, byte[][]> entry : SAMPLES.entrySet()) {
      assertAzamEncodeBytes(entry.getKey(), entry.getValue());
    }
  }

  void assertAzamDecodeAllBytes(String value, byte[]... expected) throws ParseException {
    try {
      byte[][] actual = azamDecodeAllBytes(value);
      for (int i = 0; i < expected.length; i++) {
        Assert.assertArrayEquals(expected[i], actual[i]);
      }
    } catch (IOException e) {
      Assert.fail("azamDecodeAllBytes throws IOException");
    }
  }

  @Test
  public void testAzamDecodeAllBytes() throws ParseException {
    for (Map.Entry<String, byte[][]> entry : SAMPLES.entrySet()) {
      assertAzamDecodeAllBytes(entry.getKey(), entry.getValue());
    }
  }

  @Test
  public void testAzamDecodeAllInts() throws IOException, ParseException {
    Assert.assertArrayEquals(new int[0], azamDecodeAllInts(""));
    Assert.assertArrayEquals(new int[] {0x00}, azamDecodeAllInts("0"));
    Assert.assertArrayEquals(new int[] {0x01}, azamDecodeAllInts("1"));
    Assert.assertArrayEquals(new int[] {0x0f}, azamDecodeAllInts("f"));
    Assert.assertArrayEquals(new int[] {0x10}, azamDecodeAllInts("h0"));
    Assert.assertArrayEquals(new int[] {0x11}, azamDecodeAllInts("h1"));
    Assert.assertArrayEquals(new int[] {0x1f}, azamDecodeAllInts("hf"));
    Assert.assertArrayEquals(new int[] {0xf0}, azamDecodeAllInts("z0"));
    Assert.assertArrayEquals(new int[] {0xf1}, azamDecodeAllInts("z1"));
    Assert.assertArrayEquals(new int[] {0xff}, azamDecodeAllInts("zf"));
    Assert.assertArrayEquals(new int[] {0x0100}, azamDecodeAllInts("hg0"));
    Assert.assertArrayEquals(new int[] {0x0fff}, azamDecodeAllInts("zzf"));
    Assert.assertArrayEquals(new int[] {0xffff}, azamDecodeAllInts("zzzf"));
    Assert.assertArrayEquals(new int[] {0x0fffff}, azamDecodeAllInts("zzzzf"));
    Assert.assertArrayEquals(new int[] {0xffffff}, azamDecodeAllInts("zzzzzf"));
    Assert.assertArrayEquals(new int[] {0x0fffffff}, azamDecodeAllInts("zzzzzzf"));
    Assert.assertArrayEquals(new int[] {0xffffffff}, azamDecodeAllInts("zzzzzzzf"));
  }

  void assertAcamDecodeAllIntsParseException(String... values) throws IOException {
    for (String value : values) {
      ThrowingRunnable runnable = new ThrowingRunnable() {
        String value = null;

        public ThrowingRunnable setValue(String value) {
          this.value = value;
          return this;
        }

        @Override
        public void run() throws IOException, ParseException {
          int[] unexpected = azamDecodeAllInts(value);
        }
      }.setValue(value);
      Assert.assertThrows(ParseException.class, runnable);
    }
  }

  @Test
  public void testAzamDecodeAllIntsParseException() throws IOException {
    assertAcamDecodeAllIntsParseException("h", "hh", "hhh", "_0", "gf");
  }

}
