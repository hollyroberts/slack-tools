package utils

import io.mockk.every
import io.mockk.mockk
import okhttp3.internal.toImmutableList
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.core.Appender
import org.apache.logging.log4j.core.LogEvent
import org.apache.logging.log4j.core.Logger
import java.io.Closeable

class TestLogHelper private constructor(
    private val appender: Appender,
    private val logger: Logger,
    private val logEvents: MutableList<LogEvent>

) : Closeable {
  fun events() = logEvents.toImmutableList()

  fun setLevel(level: Level) {
    logger.level = level
  }

  override fun close() {
    logger.removeAppender(appender)
  }

  companion object {
    fun forClass(clazz: Class<*>): TestLogHelper {
      val appender: Appender = mockk()
      every { appender.name } returns "TEST-" + clazz.canonicalName
      every { appender.isStarted } returns true
      every { appender.isStopped } returns false
      every { appender.stop() } returns Unit

      // We can't use capture's here because then we can't make the events immutable
      val logEvents = mutableListOf<LogEvent>()
      every { appender.append(ofType()) } answers {
        logEvents.add((firstArg() as LogEvent).toImmutable())
      }

      val logger = LogManager.getLogger(clazz)!! as Logger
      logger.addAppender(appender)

      return TestLogHelper(appender, logger, logEvents)
    }
  }
}