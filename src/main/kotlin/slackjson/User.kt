package slackjson

import com.squareup.moshi.*
import utils.iterateObject

@JsonClass(generateAdapter = true)
data class User(
        // Strings and profile
        val id: String,
        @Json(name = "team_id")
        val teamId: String,

        @Deprecated(message = "Use username() instead")
        val name: String,
        val profile: Profile,

        // Booleans
        val deleted: Boolean,
        @Deprecated(message = "Use username() instead")
        val is_bot: Boolean
) {
    @Suppress("DEPRECATION")
    fun username() = name
    fun displayname() = if (profile.displayName.isNotEmpty()) {
        profile.displayName
    } else {
        profile.realName
    }
    @Suppress("DEPRECATION")
    fun isBot() = is_bot || id == "USLACKBOT"
}

object ProfileJsonAdapter {
    /**
     * Takes a JsonReader representing a profile
     * @return a profile with display_name and map of images
     */
    @FromJson
    fun jsonToProfile(reader: JsonReader) : Profile {
        var displayName: String? = null
        var realName: String? = null
        val images = mutableMapOf<String, String>()

        // Loop to iterate over and consume object
        reader.iterateObject {
            val name = reader.nextName()

            when {
                name == "display_name" -> displayName = reader.nextString()
                name == "real_name" -> realName = reader.nextString()
                name.startsWith("image_") -> images[name.removePrefix("image_")] = reader.nextString()
                else -> reader.skipValue()
            }
        }

        // Check that we've got what we want
        if (displayName == null) {
            throw JsonDataException("No display_name for profile")
        }
        if (realName == null) {
            throw JsonDataException("No real_name for profile")
        }
        if (images.isEmpty()) {
            throw JsonDataException("No images found for profile")
        }

        return Profile(displayName!!, realName!!, images)
    }

    @ToJson
    @Suppress("UNUSED_PARAMETER")
    fun profileToJson(profile: Profile) : String {
        throw UnsupportedOperationException("Serialisation of Profile is not supported")
    }
}

data class Profile(
        val displayName: String,
        val realName: String,
        val images: Map<String, String>
) {

    /**
     * Returns the URL of the largest avatar image in the profile
     */
    fun getLargestImage() : String {
        // Try to get the original
        if (images.containsKey("original")) {
            return images.getValue("original")
        }

        // get the largest number
        return images
                .mapKeys { it.key.toIntOrNull() }
                .filterNot { it.key == null }
                .maxBy { it.key!!.toInt() }!!.value
    }
}