package slackjson

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.JsonDataException

@JsonClass(generateAdapter = true)
class Conversation(
        // Main data we're interested in
        val id: String,
        val name: String?,

        // Conversation type
        @Json(name = "is_channel")
        val isChannel: Boolean = false,
        @Json(name = "is_group")
        val isGroup: Boolean = false,
        @Json(name = "is_im")
        val isIm: Boolean = false
) {
    /**
     * Workaround because init{} doesn't work with Moshi's codegen (the object is created twice)
     * Instead we call this in ConversationList to verify all the objects
     */
    fun verify() {
        if (!isChannel && !isGroup && !isIm) {
            throw JsonDataException("Channel $id" + if (name != null) " ($name)" else ""
            + " is not a channel, private group, or dm")
        }
    }
}