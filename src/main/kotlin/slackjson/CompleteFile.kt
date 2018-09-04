package slackjson

import utils.Api
import utils.Log

class CompleteFile(pf: ParsedFile) : ParsedFile(pf) {
    init {
        val uploadLoc = when {
            channelsUploadedIn() == 1 -> channels?.firstOrNull() ?: groups?.firstOrNull() ?: ims!![0]
            channelsUploadedIn() == 0 -> {
                Log.info("File $id belongs to no channels")
                null
            }
            else -> {
                Log.debugHigh("File $id belongs to more than one channel, requires API call to resolve")
                resolveMultipleLocations(this)
            }
        }
    }


    /**
     * Parsed files can have multiple channels, we want to figure out where it was shared to first
     * Used during initialisation
     * Returns id of channel that file was first seen in
     */
    private fun resolveMultipleLocations(cf: CompleteFile) : String? {
        val f = Api.getFile(cf.id)

        with(f.firstSeen!!) {
            if (this.isNotEmpty()) {
                return this.minBy { it.value }!!.key
            }
        }

        return null
    }
}