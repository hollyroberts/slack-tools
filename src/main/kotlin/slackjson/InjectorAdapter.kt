package slackjson

import com.squareup.moshi.*
import java.lang.reflect.Type
import javax.inject.Inject
import javax.inject.Singleton


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

    @Singleton
    class Factory @Inject constructor(): JsonAdapter.Factory {
        override fun create(type: Type, annotations: MutableSet<out Annotation>, moshi: Moshi): JsonAdapter<*>? {
            if (Types.getRawType(type).isAnnotationPresent(MoshiInject::class.java)) {
                val adapter: JsonAdapter<Any> = moshi.nextAdapter(this, type, annotations)
                return InjectorAdapter(adapter)
            }

            return null
        }
    }
}