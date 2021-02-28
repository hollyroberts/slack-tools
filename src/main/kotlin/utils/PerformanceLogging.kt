package utils

import org.apache.logging.log4j.kotlin.Logging

object PerformanceLogging : Logging {
  private val runtime by lazy { Runtime.getRuntime() }

  fun outputMemoryUsage() {
    val freeMemory = runtime.freeMemory()
    val totalMemory = runtime.totalMemory()
    val usedMemory = totalMemory - freeMemory

    logger.info { "Used memory: ${formatSize(usedMemory, 4)}, Total memory: ${formatSize(totalMemory, 4)}" }
  }
}