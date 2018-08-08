package Utils

/**
 * Generic results class to reduce use of Pair
 * Eg. when interfacing with Utils.Http.get
 */
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