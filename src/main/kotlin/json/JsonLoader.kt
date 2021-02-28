package json

import com.squareup.moshi.JsonAdapter
import okio.buffer
import okio.source
import org.apache.logging.log4j.kotlin.Logging
import slack.SlackExportProcessor
import java.nio.file.Files
import java.nio.file.Path

object JsonLoader : Logging {
  fun <T> loadJson(location: Path, adapter: JsonAdapter<T>): T {
    SlackExportProcessor.logger.trace { "Loading \"$location\"" }

    val bufferedSource = Files.newInputStream(location)
        .source()
        .buffer()
    return adapter.fromJson(bufferedSource)!!
  }
}