package json

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapter

// Use this inline wrapper method for now so we can opt-in to the experimental feature without having to update the codebase
@OptIn(ExperimentalStdlibApi::class)
inline fun <reified T> Moshi.reifiedAdapter(): JsonAdapter<T> {
    return this.adapter()
}

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