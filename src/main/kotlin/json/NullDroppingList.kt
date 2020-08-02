package json

import com.squareup.moshi.*
import utils.iterateArray
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

class NullDroppingList<T>(
        list: List<T>,
        val droppedItems: Int
): ArrayList<T>(list) {
    class Adapter<T>(private val adapter: JsonAdapter<T>) : JsonAdapter<NullDroppingList<T>>() {
        override fun fromJson(reader: JsonReader): NullDroppingList<T>? {
            var droppedItems = 0
            val newList = mutableListOf<T>()

            reader.iterateArray {
                val item = adapter.fromJson(reader)

                if (item == null) {
                    droppedItems++
                } else {
                    newList.add(item)
                }
            }

            return NullDroppingList(newList, droppedItems)
        }

        override fun toJson(writer: JsonWriter, value: NullDroppingList<T>?) {
            throw UnsupportedOperationException("Can't serialise null dropping list")
        }
    }

    object Factory : JsonAdapter.Factory {
        override fun create(type: Type, annotations: MutableSet<out Annotation>, moshi: Moshi): JsonAdapter<*>? {
            val baseType = Types.getRawType(type)
            if (baseType != NullDroppingList::class.java || type !is ParameterizedType) {
                return null
            }
            if (type.actualTypeArguments.size != 1) {
                return null
            }

            val listType = type.actualTypeArguments[0]!!
            val typeAdapter: JsonAdapter<Any> = moshi.adapter(listType)

            return Adapter(typeAdapter)
        }
    }
}