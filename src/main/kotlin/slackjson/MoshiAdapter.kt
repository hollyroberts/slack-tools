package slackjson

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi

object MoshiAdapter {
    val adapter: Moshi = Moshi.Builder()
            .add(ProfileJsonAdapter)
            .add(ShareJsonAdapter)
            .build()

    fun <T> forClass(clazz: Class<T>) : JsonAdapter<T> {
        return adapter.adapter(clazz)
    }
}