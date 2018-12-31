package slack

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Types
import okio.Okio
import slackjson.User
import utils.WebApi
import utils.Log
import java.nio.file.Path

class SlackExport private constructor() {
    // Factory method
    companion object {
        fun loadFromFolder(folder: Path) : SlackExport {
            val dataType = Types.newParameterizedType(List::class.java, User::class.java)
            val adapter: JsonAdapter<List<User>> = WebApi("").moshi.adapter(dataType)!!
            val userList = loadJson(folder.resolve("users.json"), adapter)
            val userMap = userList.associateBy({it.id}, {it})

            // TODO users
            // TODO channel JSON
            // TODO channels

            return SlackExport()
        }

        private fun<T> loadJson(location: Path, adapter: JsonAdapter<T>) : T {
            Log.debugLow("Loading " + location.toString())
            val file = location.toFile()
            val bufferedSource = Okio.buffer(Okio.source(file))
            return adapter.fromJson(bufferedSource)!!
        }
    }
}