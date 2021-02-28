package utils

import org.apache.logging.log4j.Level
import org.apache.logging.log4j.core.config.Configurator
import org.apache.logging.log4j.kotlin.Logging
import scripts.CommandEnum

object Log : Logging {
  enum class SupportedLevel(override val optionName: String) : CommandEnum {
    TRACE("TRACE"),
    DEBUG("DEBUG"),
    LOW("LOW"),
    MEDIUM("MEDIUM"),
    HIGH("HIGH"),
    WARN("WARN"),
    ERROR("ERROR");
  }

  fun setLevel(mode: SupportedLevel) {
    val level = when (mode) {
      SupportedLevel.TRACE -> Level.TRACE
      SupportedLevel.DEBUG -> Level.DEBUG
      SupportedLevel.LOW -> LOW
      SupportedLevel.MEDIUM -> Level.INFO
      SupportedLevel.HIGH -> HIGH
      SupportedLevel.WARN -> Level.WARN
      SupportedLevel.ERROR -> Level.ERROR
    }

    Configurator.setRootLevel(level)
    logger.debug { "Set root level to $level (${level.intLevel()})" }
  }

  // Log4j2 uniquely identifies level by name not level sadly
  // So to have multiple levels with the same name but different levels we have to use zero width spaces to make them technically different
  val LOW = Level.forName("INFO\u200B", 410)!!
  val HIGH = Level.forName("INFO\u200B\u200B", 390)!!

  // TODO add extension functions for above? Can it be done with static dispatch?
}