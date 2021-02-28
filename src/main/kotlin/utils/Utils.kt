package utils

import com.squareup.moshi.JsonReader
import com.squareup.moshi.Moshi
import okio.Buffer
import org.apache.logging.log4j.kotlin.logger
import java.lang.Long.numberOfLeadingZeros
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import java.net.URLConnection
import java.nio.file.Path
import java.text.DecimalFormat
import kotlin.math.max
import kotlin.math.pow

private val logger = logger("Utils")

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
fun prettyFormat(json: String): String {
  val source = Buffer().writeUtf8(json)
  val reader = JsonReader.of(source)
  val value = reader.readJsonValue()
  val adapter = Moshi.Builder().build().adapter(Any::class.java).indent("    ")
  return adapter.toJson(value)
}

/**
 * Formats the long value into a human readable version
 * @param sigFigures Significant figures to show (integer + decimal component).
 * The integer component will not lose precision if it contains more significant figures
 */
fun formatSize(size: Long, sigFigures: Int = 3): String {
  require(sigFigures >= 0) { "Precision must be greater than or equal to zero. Was: $sigFigures" }
  require(size >= 0) { "Size to format must be greater than or equal to zero. Was: $size" }

  if (size < 1024) {
    return "$size B"
  }

  // A modified strategy based off the idea of counting leading zeros from: https://stackoverflow.com/a/24805871
  val magnitude = (63 - numberOfLeadingZeros(size)) / 10
  val unitPrefix = "KMGTPE"[magnitude - 1]

  // Instead of directly converting to double, truncate to an intermediate so we're working with values < 1024^2
  val truncatedSize = size / (1L shl ((magnitude - 1) * 10))
  val finalDouble = truncatedSize.toDouble() / 1024

  // If it's above our precision value then don't display any decimal places
  if (finalDouble >= 10.0.pow(sigFigures - 1)) {
    return "${finalDouble.toInt()} ${unitPrefix}iB"
  }

  // Otherwise display at least 1
  val mathContext = MathContext(sigFigures, RoundingMode.DOWN)
  val bigDecimal = BigDecimal(finalDouble, mathContext)
  val formatter = DecimalFormat("#.0" + "#".repeat(max(0, sigFigures - 2)))
  return "${formatter.format(bigDecimal)} ${unitPrefix}iB"
}

/**
 * If the folder doesn't exist, then create it
 */
fun ensureFolderExists(location: Path) {
  if (!location.toFile().exists()) {
    logger.debug { "Creating directory ' ${location.fileName}'" }
    location.toFile().mkdirs()
  }
}

/**
 * Guess image extension from URL. Contains dot prefixed
 * Converts to lowercase and also converts jpeg -> jpg
 */
fun guessImageExtFromURL(url: String): String {
  val guess = URLConnection.guessContentTypeFromName(url) ?: return ""
  if (!guess.startsWith("image/")) return ""

  var filetype = guess.removePrefix("image/").toLowerCase()
  if (filetype == "jpeg") {
    filetype = "jpg"
  }

  return ".$filetype"
}

fun bytesToHex(bytes: ByteArray): String {
  val builder = StringBuilder()
  for (b in bytes) {
    builder.append(String.format("%02x", b))
  }
  return builder.toString()
}

/**
 * Splits the input string into stem + extension
 */
fun renameFilename(fileName: String, suffix: String): String {
  val dotLoc = fileName.lastIndexOf('.')
  val separator = if (dotLoc == -1) "." else ""

  val startAndExt = when (dotLoc) {
    -1 -> "" to ""
    else -> fileName.substring(0, dotLoc) to fileName.substring(dotLoc)
  }

  return startAndExt.first + suffix + separator + startAndExt.second
}
