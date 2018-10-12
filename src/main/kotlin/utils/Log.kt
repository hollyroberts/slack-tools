package utils

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
    val mode = Modes.HIGH

    private fun log(mode: Modes, message: String) {
        if (mode >= Log.mode) {
            val msg = Http.token?.let {
                message.replace(it, "T-O-K-E-N")
            } ?: message

            println("[${mode.tag}] $msg")
        }
    }

    fun debugLow(message: String) = log(Modes.DEBUG_LOW, message)
    fun debugHigh(message: String) = log(Modes.DEBUG_HIGH, message)
    fun low(message: String) = log(Modes.LOW, message)
    fun medium(message: String) = log(Modes.MEDIUM, message)
    fun high(message: String) = log(Modes.HIGH, message)
    fun warn(message: String) = log(Modes.WARN, message)
    fun error(message: String) = log(Modes.ERROR, message)
}