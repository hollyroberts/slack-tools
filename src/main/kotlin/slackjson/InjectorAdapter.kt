package slackjson

import com.squareup.moshi.*
import dagger.MainComponent
import java.lang.reflect.Type
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton


class InjectorAdapter<T>(
        private val delegate: JsonAdapter<T>,
        private val injectFun: (T) -> Unit
) : JsonAdapter<T>() {

    override fun fromJson(reader: JsonReader): T? {
        val obj: T = delegate.fromJson(reader)!!
        injectFun.invoke(obj)
        return obj
    }

    @ToJson
    override fun toJson(writer: JsonWriter, value: T?) {
        throw UnsupportedOperationException()
    }

    @Singleton
    class Factory @Inject constructor(componentProvider: Provider<MainComponent>) : JsonAdapter.Factory {
        private val moshiComponent = componentProvider.get()

        private val injectMap: Map<Class<*>, (Any) -> Unit> = mapOf(
                // The current compiler can't infer this, so we need the it ->
                Conversation::class.java to { it -> moshiComponent.inject(it as Conversation) },
                ParsedFile::class.java to { it -> moshiComponent.inject(it as ParsedFile) }
        )

        override fun create(type: Type, annotations: MutableSet<out Annotation>, moshi: Moshi): JsonAdapter<*>? {
            val javaType = Types.getRawType(type)
            if (!javaType.isAnnotationPresent(MoshiInject::class.java)) {
                return null
            }
            val adapter: JsonAdapter<Any> = moshi.nextAdapter(this, type, annotations)

            val injectFun: (Any) -> Unit = injectMap[javaType] ?: error("No injection method defined for $javaType")
            return InjectorAdapter(adapter, injectFun)
        }
    }
}