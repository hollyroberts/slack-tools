package jmh

import com.squareup.moshi.*
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
open class JsonSelectBenchmark {

    companion object Constants {
        private const val INNER_LOOPS = 10_000_000
        private const val VALUE_STRING = "message"
    }

    private lateinit var dataMatch: String
    private lateinit var dataNoMatch: String

    private val nextNameAdapter = Moshi.Builder()
            .add(NextNameAdapter)
            .build()
            .adapter(Int::class.java)
    private val selectNameAdapter = Moshi.Builder()
            .add(SelectNameAdapter)
            .build()
            .adapter(Int::class.java)

    @Setup
    fun setup() {
        dataMatch = this::class.java.classLoader
                .getResourceAsStream("jmh/json-name-read-success.json")!!
                .readAllBytes()
                .decodeToString()

        dataNoMatch = this::class.java.classLoader
                .getResourceAsStream("jmh/json-name-read-fail.json")!!
                .readAllBytes()
                .decodeToString()
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    fun matchNextName(blackhole: Blackhole) {
        repeat(INNER_LOOPS) {
            blackhole.consume(nextNameAdapter.fromJson(dataMatch))
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
    fun noMatchNextName(blackhole: Blackhole) {
        repeat(INNER_LOOPS) {
            blackhole.consume(nextNameAdapter.fromJson(dataNoMatch))
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
        val options = JsonReader.Options.of(VALUE_STRING)!!

        @FromJson
        fun fromJson(reader: JsonReader): Int {
            reader.beginObject()
            reader.skipName()
            val int = reader.selectString(options)
            if (int == -1) {
                reader.skipValue()
            }
            reader.endObject()
            return int
        }

        @ToJson
        fun toJson(jsonWriter: JsonWriter, value: Int) {
            throw UnsupportedOperationException()
        }
    }

    private object NextNameAdapter {
        @FromJson
        fun fromJson(reader: JsonReader): Int {
            reader.beginObject()
            reader.skipName()
            val str = reader.nextString()!!
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
}

private fun main() {
    val opt = OptionsBuilder()
            .include(JsonSelectBenchmark::class.java.simpleName)
            .build()
    Runner(opt).run()
}