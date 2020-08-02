package json

import com.squareup.moshi.JsonAdapter
import okio.buffer
import okio.source
import org.apache.logging.log4j.kotlin.Logging
import slack.SlackExportProcessor
import java.nio.file.Path

object JsonLoader : Logging {
    fun <T> loadJson(location: Path, adapter: JsonAdapter<T>): T {
        SlackExportProcessor.logger.trace { "Loading \"$location\"" }
        val file = location.toFile()
        val bufferedSource = file.source().buffer()
        return adapter.fromJson(bufferedSource)!!
    }
}