package slack

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Types
import okio.buffer
import okio.source
import slackjson.User
import utils.Log
import java.nio.file.Path

class SlackExport private constructor() {
    // Factory method
    companion object {
        fun loadFromFolder(folder: Path): SlackExport {
            val dataType = Types.newParameterizedType(List::class.java, User::class.java)
//            val adapter: JsonAdapter<List<User>> = MoshiAdapter.adapter.adapter(dataType)
//            val userList = loadJson(folder.resolve("users.json"), adapter)
//            val userMap = userList.associateBy({ it.id }, { it })

            // TODO users
            // TODO channel JSON
            // TODO channels

            return SlackExport()
        }

        private fun <T> loadJson(location: Path, adapter: JsonAdapter<T>): T {
            Log.debugLow("Loading $location")
            val file = location.toFile()
            val bufferedSource = file.source().buffer()
            return adapter.fromJson(bufferedSource)!!
        }
    }
}