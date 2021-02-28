import dagger.DaggerExportMainComponent
import dagger.ExportMainComponent
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.core.config.Configurator
import slack.Settings
import utils.Log
import utils.PerformanceLogging
import java.nio.file.Path
import kotlin.system.measureTimeMillis

fun main() = FolderProcessTest.process()

object FolderProcessTest {
  fun process() {
    val path = Path.of(System.getenv("SLACK_LOCATION"))
    val fs = InMemoryDirectory.loadDirectory(path)

    PerformanceLogging.outputMemoryUsage()

    val dagger = DaggerExportMainComponent.builder()
        .folderLocation(fs.getPath(""))
        .settings(Settings())
        .build()

    for (i in 1..4) {
      System.gc()
      timeLoadingOfExport(dagger)
      PerformanceLogging.outputMemoryUsage()
    }
  }

  private fun timeLoadingOfExport(exportComponent: ExportMainComponent) {
    Configurator.setRootLevel(Level.OFF)

    val millis = measureTimeMillis {
      val userAndConvoMap = exportComponent.getUserAndConvoMap()
      val stats = userAndConvoMap.conversations.values
          .map { exportComponent.getExportProcessor().loadConversationFolder(it) }

      Configurator.reconfigure()

      val loadedMessages = stats.sumBy { it.messagesLoaded }
      Log.logger.info { String.format("Loaded %,d messages", loadedMessages) }
    }

    Log.logger.info { String.format("Processed entire slack export in %,dms", millis) }
  }
}