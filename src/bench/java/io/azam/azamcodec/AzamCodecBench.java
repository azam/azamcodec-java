package io.azam.azamcodec;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.text.ParseException;
import java.util.concurrent.TimeUnit;

import static io.azam.azamcodec.AzamCodec.*;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
@Fork(value = 1, jvmArgs = {"-Xms256M", "-Xmx256M"})
@Warmup(iterations = 1)
@Measurement(iterations = 1)
public class AzamCodecBench {
  public static void main(String[] args) throws RunnerException {
    Options opt = new OptionsBuilder().include(AzamCodecBench.class.getSimpleName()).build();
    new Runner(opt).run();
  }

  @Benchmark
  public void azamDecodeInts1(Blackhole bh) throws ParseException {
    bh.consume(azamDecodeInts("zzzzzzzf"));
  }

  @Benchmark
  public void azamDecodeInts2(Blackhole bh) throws ParseException {
    bh.consume(azamDecodeInts("zzzzzzzfzzzzzzzf"));
  }

  @Benchmark
  public void azamDecodeInts3(Blackhole bh) throws ParseException {
    bh.consume(azamDecodeInts("zzzzzzzfzzzzzzzfzzzzzzzf"));
  }

  @Benchmark
  public void azamDecodeInts4(Blackhole bh) throws ParseException {
    bh.consume(azamDecodeInts("zzzzzzzfzzzzzzzfzzzzzzzfzzzzzzzf"));
  }

  @Benchmark
  public void azamDecodeInts5(Blackhole bh) throws ParseException {
    bh.consume(azamDecodeInts("zzzzzzzfzzzzzzzfzzzzzzzfzzzzzzzfzzzzzzzf"));
  }

  @Benchmark
  public void azamEncodeInts1(Blackhole bh) throws ParseException {
    bh.consume(azamEncodeInts(0xffffffff));
  }

  @Benchmark
  public void azamEncodeInts2(Blackhole bh) throws ParseException {
    bh.consume(azamEncodeInts(0xffffffff, 0xffffffff));
  }

  @Benchmark
  public void azamEncodeInts3(Blackhole bh) throws ParseException {
    bh.consume(azamEncodeInts(0xffffffff, 0xffffffff, 0xffffffff));
  }

  @Benchmark
  public void azamEncodeInts4(Blackhole bh) throws ParseException {
    bh.consume(azamEncodeInts(0xffffffff, 0xffffffff, 0xffffffff, 0xffffffff));
  }

  @Benchmark
  public void azamEncodeInts5(Blackhole bh) throws ParseException {
    bh.consume(azamEncodeInts(0xffffffff, 0xffffffff, 0xffffffff, 0xffffffff, 0xffffffff));
  }
}
