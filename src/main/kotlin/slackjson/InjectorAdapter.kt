package slackjson

import com.squareup.moshi.*
import dagger.MembersInjector
import java.lang.reflect.Type
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton
import kotlin.reflect.KClass

/**
 * The parameterized type here actually doesn't do anything, as it gets erased at runtime
 * Also the factory method just creates instances that are 'Any' rather than 'T'
 * But it's nice to have, and the factory method ensures the adapter is only used for the correct types
 */
class InjectorAdapter<T : Any>(
        private val injectionMap: Map<KClass<T>, MembersInjector<T>>,
        private val delegate: JsonAdapter<T>
) : JsonAdapter<T>() {

    override fun fromJson(reader: JsonReader): T? {
        val obj: T = delegate.fromJson(reader)!!
        injectionMap[obj::class]?.injectMembers(obj)
        return obj
    }

    @ToJson
    override fun toJson(writer: JsonWriter, value: T?) {
        throw UnsupportedOperationException("Serialisation of injected objects is not supported")
    }

    @Suppress("RemoveRedundantQualifierName")
    @Singleton
    class JsonFactory @Inject constructor(
            private val injectorMap: Map<Class<*>, @JvmSuppressWildcards Provider<InjectionMap>>
    ) : JsonAdapter.Factory {

        override fun create(type: Type, annotations: MutableSet<out Annotation>, moshi: Moshi): JsonAdapter<*>? {
            val javaType = Types.getRawType(type)
            if (!javaType.isAnnotationPresent(MoshiInject::class.java)) {
                return null
            }
            val adapter: JsonAdapter<Any> = moshi.nextAdapter(this, type, annotations)
            val injector = injectorMap[javaType]?.get() ?: error("No injection provider defined in map multibinder for $javaType")

            @Suppress("UNCHECKED_CAST")
            return InjectorAdapter(injector as Map<KClass<Any>, MembersInjector<Any>>, adapter)
        }
    }
}