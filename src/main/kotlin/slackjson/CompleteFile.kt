package slackjson

import utils.Api
import utils.Log

class CompleteFile(pf: ParsedFile) : ParsedFile(pf) {
    var uploadLoc: String? = null

    init {
        when {
            channelsUploadedIn() == 1 -> uploadLoc = channels?.firstOrNull() ?: groups?.firstOrNull() ?: ims!![0]
            channelsUploadedIn() == 0 -> Log.info("File $id belongs to no channels")
            else -> {
                Log.debugHigh("File $id belongs to more than one channel, requires API call to resolve")
                resolveMultipleLocations(this)
            }
        }
    }


    /**
     * Parsed files can have multiple channels, we want to figure out where it was shared to first
     * Used during initialisation
     */
    private fun resolveMultipleLocations(cf: CompleteFile) {
        val f = Api.getFile(cf.id)

        with(f.firstSeen!!) {
            if (this.isNotEmpty()) {
                cf.uploadLoc = this.minBy { it.value }!!.key
            }
        }
    }
}