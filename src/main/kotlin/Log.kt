object Log {
    enum class Modes { DEBUG, INFO, WARN, ERROR }
    val mode = Modes.DEBUG

    private fun log(mode: Modes, message: String) {
        if (mode >= this.mode) {
            println("[${mode.name}] $message")
        }
    }

    fun debug(message: String) = log(Modes.DEBUG, message)
    fun info(message: String) = log(Modes.INFO, message)
    fun warn(message: String) = log(Modes.WARN, message)
    fun error(message: String) = log(Modes.ERROR, message)
}