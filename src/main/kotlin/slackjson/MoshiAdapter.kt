package slackjson

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import slackjson.message.BaseMessageCustomAdapter

object MoshiAdapter {
    val adapter: Moshi by lazy {
        Moshi.Builder()
                .add(BaseMessageCustomAdapter)
                .add(ProfileJsonAdapter)
                .add(ShareJsonAdapter)
                .build()
    }

    fun <T> forClass(clazz: Class<T>) : JsonAdapter<T> {
        return adapter.adapter(clazz)
    }
}