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
        val isIm: Boolean = false,

        // User field if dm
        val user: String?
) {
    /**
     * Workaround because init{} doesn't work with Moshi's codegen (the object is created twice)
     * Instead we call this in ConversationList to verify all the objects
     */
    fun verify() {
        // Must be a channel, group, or dm
        if (!isChannel && !isGroup && !isIm) {
            throw JsonDataException("Channel $id" + if (name != null) " ($name)" else ""
            + " is not a channel, private group, or dm")
        }

        // If it's a dm, then we must know the user
        if (isIm && user == null) {
            throw JsonDataException("Conversation $id is a dm, but does not contain a user field")
        }
    }

    /**
     * Returns conversation name prefixed with # or @ depending on whether it's a channel or dm
     * TODO convert ids
     */
    fun getFullName() : String {
        return if (isChannel || isGroup) {
            "#$id"
        } else {
            "@$user"
        }
    }
}