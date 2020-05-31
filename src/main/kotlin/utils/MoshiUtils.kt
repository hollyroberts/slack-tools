package utils

import com.squareup.moshi.JsonReader

/**
 * Consumes an object from a reader and iterates over it, calling the function while there are still entries
 */
inline fun JsonReader.iterateObject(function: () -> Unit) {
    this.beginObject()
    while (this.hasNext()) {
        function.invoke()
    }
    this.endObject()
}

/**
 * Consumes an array from a reader and iterates over it, calling the function while there are still entries
 */
inline fun JsonReader.iterateArray(function: () -> Unit) {
    this.beginArray()
    while (this.hasNext()) {
        function.invoke()
    }
    this.endArray()
}