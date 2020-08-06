package json.slack.message

import org.apache.logging.log4j.kotlin.Logging
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MessageTypeRecorder @Inject constructor() {
    private val typeMap = mutableMapOf<String?, Int>()

    companion object : Logging

    fun recordType(type: String?) {
        typeMap.compute(type) { _, v -> if (v == null) 1 else v + 1 }
    }

    fun logResults() {
        typeMap.mapKeys { it.key ?: "No subtype" }
                .entries
                .sortedByDescending { it.value }
                .forEach {
                    logger.info { String.format("%-20s %,d", it.key, it.value) }
                }

    }

    private fun typeCount(type: String?): Int {
        return typeMap.getOrDefault(type, 0)
    }


}