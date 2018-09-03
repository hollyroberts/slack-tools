package utils

object Log {
    enum class Modes { DEBUG_LOW, DEBUG_HIGH, INFO, WARN, ERROR }
    val mode = Modes.DEBUG_LOW

    private fun log(mode: Modes, message: String) {
        if (mode >= Log.mode) {
            val msg = Http.token?.let {
                message.replace(it, "T-O-K-E-N")
            } ?: message

            println("[${mode.name}] $msg")
        }
    }

    fun debugLow(message: String) = log(Modes.DEBUG_LOW, message)
    fun debugHigh(message: String) = log(Modes.DEBUG_HIGH, message)
    fun info(message: String) = log(Modes.INFO, message)
    fun warn(message: String) = log(Modes.WARN, message)
    fun error(message: String) = log(Modes.ERROR, message)
}