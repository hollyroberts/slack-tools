package utils

import org.apache.logging.log4j.Level
import org.apache.logging.log4j.core.config.Configurator

object Log {
    enum class Modes(val tag: String) {
        // All tags should be 4 chars to fit nicely
        // The exception to this is error, however drawing more attention to the message is a plus
        DEBUG_LOW("D-LO"),
        DEBUG_HIGH("D-HI"),
        LOW("INFO"),
        MEDIUM("INFO"),
        HIGH("INFO"), // Minimal logging to cut down on output, but there may be a large wait between output
        WARN("WARN"),
        ERROR("ERROR")
    }

    fun argStringMap() : LinkedHashMap<String, Modes> {
        val values = Modes.values().map{ it.name.replace("_", "-") to it }
        return linkedMapOf(*values.toTypedArray())
    }

    fun setLevel(mode: Modes) {
        val level = when (mode) {
            Modes.DEBUG_LOW -> Level.TRACE
            Modes.DEBUG_HIGH -> Level.DEBUG
            Modes.LOW -> LOW
            Modes.MEDIUM -> Level.INFO
            Modes.HIGH -> HIGH
            Modes.WARN -> Level.WARN
            Modes.ERROR -> Level.ERROR
        }

        println(HIGH.intLevel())
        println("Setting level to ${level.intLevel()}")
        Configurator.setRootLevel(level)
    }

    // Log4j2 uniquely identifies level by name not level sadly
    // So to have multiple levels with the same name but different levels we have to use zero width spaces to make them technically different
    val LOW = Level.forName("INFO\u200B", 410)!!
    val HIGH = Level.forName("INFO\u200B\u200B", 390)!!
}