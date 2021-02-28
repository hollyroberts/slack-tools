package jmh

import com.squareup.moshi.*
import json.slack.message.ChannelEvent
import org.openjdk.jmh.annotations.*
import org.openjdk.jmh.infra.Blackhole
import org.openjdk.jmh.runner.Runner
import org.openjdk.jmh.runner.options.OptionsBuilder
import java.util.concurrent.TimeUnit

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
@Fork(value = 3, warmups = 0, jvmArgsAppend = ["-XX:-BackgroundCompilation"])
@Warmup(iterations = 10, time = 1)
@Measurement(iterations = 5, time = 1)
open class JsonSelectKeyBenchmark {

  companion object Constants {
    private const val INNER_LOOPS = 10_000_000
    private const val VALUE_STRING = "channel_archive"

    private val EVENTS_LOOKUP = ChannelEvent.values().associate { it.label to it.ordinal }
  }

  private lateinit var dataMatch: String
  private lateinit var dataNoMatch: String

  private val nextNameAdapterSingle = Moshi.Builder()
      .add(NextNameAdapterSingle)
      .build()
      .adapter(Int::class.java)
  private val nextNameAdapterLookup = Moshi.Builder()
      .add(NextNameAdapterLookup)
      .build()
      .adapter(Int::class.java)
  private val selectNameAdapter = Moshi.Builder()
      .add(SelectNameAdapter)
      .build()
      .adapter(Int::class.java)

  @Setup
  fun setup() {
    dataMatch = this::class.java.classLoader
        .getResourceAsStream("jmh/json-key-read-success.json")!!
        .readAllBytes()
        .decodeToString()

    dataNoMatch = this::class.java.classLoader
        .getResourceAsStream("jmh/json-key-read-fail.json")!!
        .readAllBytes()
        .decodeToString()
  }

  @Benchmark
  @BenchmarkMode(Mode.AverageTime)
  fun matchNextNameSingle(blackhole: Blackhole) {
    repeat(INNER_LOOPS) {
      blackhole.consume(nextNameAdapterSingle.fromJson(dataMatch))
    }
  }

  @Benchmark
  @BenchmarkMode(Mode.AverageTime)
  fun matchNextNameLookup(blackhole: Blackhole) {
    repeat(INNER_LOOPS) {
      blackhole.consume(nextNameAdapterLookup.fromJson(dataMatch))
    }
  }

  @Benchmark
  @BenchmarkMode(Mode.AverageTime)
  fun matchSelectName(blackhole: Blackhole) {
    repeat(INNER_LOOPS) {
      blackhole.consume(selectNameAdapter.fromJson(dataMatch))
    }
  }

  @Benchmark
  @BenchmarkMode(Mode.AverageTime)
  fun noMatchNextNameSingle(blackhole: Blackhole) {
    repeat(INNER_LOOPS) {
      blackhole.consume(nextNameAdapterSingle.fromJson(dataNoMatch))
    }
  }

  @Benchmark
  @BenchmarkMode(Mode.AverageTime)
  fun noMatchNextNameLookup(blackhole: Blackhole) {
    repeat(INNER_LOOPS) {
      blackhole.consume(nextNameAdapterLookup.fromJson(dataNoMatch))
    }
  }

  @Benchmark
  @BenchmarkMode(Mode.AverageTime)
  fun noMatchSelectName(blackhole: Blackhole) {
    repeat(INNER_LOOPS) {
      blackhole.consume(selectNameAdapter.fromJson(dataNoMatch))
    }
  }

  private object SelectNameAdapter {
    private val options = JsonReader.Options.of(*ChannelEvent.values().map { it.label }.toTypedArray())!!

    @FromJson
    fun fromJson(reader: JsonReader): Int {
      reader.beginObject()
      val int = reader.selectName(options)
      if (int == -1) {
        reader.skipName()
      }

      reader.skipValue()
      reader.endObject()

      return int
    }

    @ToJson
    fun toJson(jsonWriter: JsonWriter, value: Int) {
      throw UnsupportedOperationException()
    }
  }

  private object NextNameAdapterSingle {
    @FromJson
    fun fromJson(reader: JsonReader): Int {
      reader.beginObject()
      val str = reader.nextName()!!
      reader.skipValue()
      reader.endObject()

      if (str == VALUE_STRING) {
        return 0
      }
      return -1
    }

    @ToJson
    fun toJson(jsonWriter: JsonWriter, value: Int) {
      throw UnsupportedOperationException()
    }
  }

  private object NextNameAdapterLookup {
    @FromJson
    fun fromJson(reader: JsonReader): Int {
      reader.beginObject()
      val str = reader.nextName()!!
      reader.skipValue()
      reader.endObject()

      return EVENTS_LOOKUP[str] ?: -1
    }

    @ToJson
    fun toJson(jsonWriter: JsonWriter, value: Int) {
      throw UnsupportedOperationException()
    }
  }
}

private fun main() {
  val opt = OptionsBuilder()
      .include(JsonSelectKeyBenchmark::class.java.simpleName)
      .build()
  Runner(opt).run()
}