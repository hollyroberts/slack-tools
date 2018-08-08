package slack

import com.squareup.moshi.*
import com.squareup.moshi.JsonReader

data class User(
        // Strings and profile
        val id: String,
        val team_id: String,
        val name: String,
        val profile: Profile,

        // Booleans
        val is_bot: Boolean
)

object ProfileJsonAdapter {
    /**
     * Takes a JsonReader representing a profile
     * @return a profile with display_name and map of images
     */
    @FromJson fun jsonToProfile(reader: JsonReader) : Profile {
        var display_name: String? = null
        val images = mutableMapOf<String, String>()

        // Loop to iterate over and consume object
        reader.beginObject()
        while(reader.peek() != JsonReader.Token.END_OBJECT) {
            val name = reader.nextName()

            when {
                name == "display_name" -> display_name = reader.nextString()
                name.startsWith("image_") -> images[name.removePrefix("image_")] = reader.nextString()
                else -> reader.skipValue()
            }
        }
        reader.endObject()

        // Check that we've got what we want
        if (display_name == null) {
            throw JsonDataException("No display_name for profile")
        }
        if (images.isEmpty()) {
            throw JsonDataException("No images found for profile")
        }

        return Profile(display_name, images)
    }
}

data class Profile(
        val display_name: String,
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
                .filterKeys { when(it.toIntOrNull()){
                    null -> false
                    else -> true
                }}
                .maxBy { it.key.toInt() }!!.value
    }
}