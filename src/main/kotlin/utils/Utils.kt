package utils

import sun.plugin2.util.PojoUtil.toJson
import com.squareup.moshi.Moshi
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import okio.Buffer
import java.text.DecimalFormat
import kotlin.math.log
import kotlin.math.max
import kotlin.math.pow


/**
 * Generic results class to reduce use of Pair
 * Eg. when interfacing with utils.Http.get
 */
@Suppress("unused")
sealed class Result<out R> {
    data class Success<out T>(val value: T) : Result<T>()
    data class Failure(val msg: String)
}

fun ordinal(number: Int): String {
    val suffixes = arrayOf("th", "st", "nd", "rd", "th", "th", "th", "th", "th", "th")
    return when (number % 100) {
        11, 12, 13 -> number.toString() + "th"
        else -> number.toString() + suffixes[number % 10]
    }
}

/**
 * Prettifies json string to be nicely indented
 */
fun prettyFormat(json: String) : String {
    val source = Buffer().writeUtf8(json)
    val reader = JsonReader.of(source)
    val value = reader.readJsonValue()
    val adapter = Moshi.Builder().build().adapter(Any::class.java).indent("    ")
    return adapter.toJson(value)
}

/**
 * Formats the size of the file into a human readable version
 * @param precision Number of decimal places to return (there will be at least 1)
 */
fun formatSize(size: Long, precision: Int = 2) : String {
    if (size < 1024) return "$size B"
    val exp = log(size.toDouble(), 1024.0).toInt()
    val prefix = "KMGTPE"[exp - 1]

    val df = DecimalFormat("#.0" + "#".repeat(max(0, precision - 1)))
    val formattedSize = df.format(size / (1024.0).pow(exp))
    return "$formattedSize ${prefix}iB"
}