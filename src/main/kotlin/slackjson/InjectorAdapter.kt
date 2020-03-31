package slackjson

import com.squareup.moshi.*
import java.lang.reflect.Type

interface MoshiInjectable

class InjectorAdapter<T>(private val delegate: JsonAdapter<T>) : JsonAdapter<T>() {
    override fun fromJson(reader: JsonReader): T? {
        val obj: T = delegate.fromJson(reader)!!
        println("Hello world!")
        return obj
    }

    @ToJson
    override fun toJson(writer: JsonWriter, value: T?) {
        throw UnsupportedOperationException()
    }

    companion object : Factory {
        override fun create(type: Type, annotations: MutableSet<out Annotation>, moshi: Moshi): JsonAdapter<*>? {
            if (MoshiInjectable::class.java.isAssignableFrom(Types.getRawType(type))) {
                val adapter: JsonAdapter<Any> = moshi.nextAdapter(this, type, annotations)
                return InjectorAdapter(adapter)
            }

            return null
        }
    }
}