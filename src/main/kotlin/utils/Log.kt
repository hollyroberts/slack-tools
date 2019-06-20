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
    @Suppress("MemberVisibilityCanBePrivate")
    var mode = Modes.MEDIUM

    private val tokenSet = mutableSetOf<String>()

    fun argStringMap() : LinkedHashMap<String, Modes> {
        val values = Modes.values().map{ it.name.replace("_", "-") to it }
        return linkedMapOf(*values.toTypedArray())
    }

    /**
     * Add a 'token' to the token set
     * Every instance of the token will be replaced with 'T-O-K-E-N' when encountered in logging
     * Used to remove sensitive information from logs
     *
     * Note that if the token is too small (eg. length less than 10) then it is not added to the set
     */
    fun addToken(token: String) {
        // Ignore any token below an arbitrary length. I think tokens are about 30 chars long
        if (token.length >= 10) {
            tokenSet.add(token)
        }
    }

    fun log(mode: Modes, message: String) {
        if (mode < Log.mode) {
            return
        }

        var msg = message
        for (token in tokenSet) {
            msg = msg.replace(token, "T-O-K-E-N")
        }

        println("[${mode.tag}] $msg")
    }

    fun debugLow(message: String) = log(Modes.DEBUG_LOW, message)
    fun debugHigh(message: String) = log(Modes.DEBUG_HIGH, message)
    fun low(message: String) = log(Modes.LOW, message)
    fun medium(message: String) = log(Modes.MEDIUM, message)
    fun high(message: String) = log(Modes.HIGH, message)
    fun warn(message: String) = log(Modes.WARN, message)
    fun error(message: String) = log(Modes.ERROR, message)
}