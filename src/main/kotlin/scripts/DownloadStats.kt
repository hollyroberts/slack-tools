package scripts

import slackjson.DownloadStatus
import utils.Log

/**
 * Records occurrences of enum
 */
@Suppress("MemberVisibilityCanBePrivate")
class DownloadStats {
    private val map = mutableMapOf<DownloadStatus, Int>()

    init {
        DownloadStatus.values().forEach {
            map[it] = 0
        }
    }

    // Functions for collecting data
    fun update(status: DownloadStatus) {
        map[status] = map[status]!! + 1
    }

    operator fun plus(other: DownloadStats) : DownloadStats {
        val new = DownloadStats()

        DownloadStatus.values().forEach {
            new.map[it] = map[it]!! + other.map[it]!!
        }

        return new
    }

    // Data retrieval methods
    fun links() = map[DownloadStatus.LINK]!!

    fun successes() = map[DownloadStatus.SUCCESS]!! +
            map[DownloadStatus.SUCCESS_OVERWRITE]!! +
            map[DownloadStatus.ALREADY_EXISTED]!! +
            links()

    fun failures() = map[DownloadStatus.FAILURE]!!

    fun total() = successes() + failures()

    fun getMessage(location: String) : String {
        var msg = "Downloaded ${successes()}/${total()} files successfully from $location"

        if (links() > 0) {
            msg += " (of which ${links()} were links and saved to text files)"
        }

        return msg
    }

    fun log(location: String) {
        // Output information
        if (failures() == 0) {
            Log.medium(getMessage(location))
        } else {
            Log.warn(getMessage(location))
        }
    }
}