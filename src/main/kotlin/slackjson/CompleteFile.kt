package slackjson

import utils.Api
import utils.Log

class CompleteFile private constructor() : ParsedFile() {
    var uploadLoc: String? = null

    companion object {
        /**
         * Take a parsed file and perform the appropriate actions to make it whole
         */
        fun create(pf: ParsedFile) : CompleteFile {
            // Cast from parsed file to complete file to keep properties
            val cf = pf as CompleteFile

            with(cf) {
                // Note: If channels uploaded in is 0, then we don't know where the file was uploaded to
                // This can happen if the file is uploaded, and then the file message is deleted

                when {
                    channelsUploadedIn() == 1 -> uploadLoc = channels?.firstOrNull() ?: groups?.firstOrNull() ?: ims!![0]
                    channelsUploadedIn() == 0 -> Log.debugHigh("File ${cf.id} has no channels")
                    else -> resolveMultipleLocations(this)
                }
            }

            return cf
        }

        /**
         * Parsed files can have multiple channels, we want to figure out where it was shared to first
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
}