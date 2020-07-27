package utils

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import java.lang.reflect.Type
import kotlin.reflect.KType
import kotlin.reflect.KTypeProjection
import kotlin.reflect.jvm.jvmErasure
import kotlin.reflect.typeOf

// TODO hopefully at some point Moshi will implement this for us
// They already have this: https://github.com/square/moshi/blob/master/kotlin/tests/src/test/kotlin/com/squareup/moshi/kotlin/reflect/-MoshiKotlinExtensions.kt
// We could just pull it in (it's probably better), but making this was interesting
// It'd also be harder to debug for the edge cases, so lets wait until something is stable
@OptIn(ExperimentalStdlibApi::class)
inline fun <reified T> Moshi.reifiedAdapter(): JsonAdapter<T> {
    val moshiType = getMoshiType(typeOf<T>())
    return this.adapter(moshiType)
}

@PublishedApi
internal fun getMoshiType(type: KType): Type {
    if (type.arguments.isEmpty()) {
        return type.jvmErasure.javaObjectType
    }

    val typeParameters = mutableListOf<Type>()
    for (typeProjection: KTypeProjection in type.arguments) {
        if (typeProjection.type == null) {
            throw IllegalArgumentException("Type projection $typeProjection has null type. This can happen if star projection is used (java)")
        }

        typeParameters.add(getMoshiType(typeProjection.type!!))
    }

    return Types.newParameterizedType(type.jvmErasure.java, *typeParameters.toTypedArray())
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