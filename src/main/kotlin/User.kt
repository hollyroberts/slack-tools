import com.beust.klaxon.*

data class User(
        // Strings and profile
        val id: String,
        val team_id: String,
        val name: String,
        val profile: Profile,

        // Booleans
        val is_bot: Boolean
)

object ProfileConverter: Converter {
    // Don't support converting from object to JSON
    override fun toJson(value: Any): String {
        return "Not implemented"
    }

    override fun canConvert(cls: Class<*>) = cls == Profile::class.java
    override fun fromJson(jv: JsonValue): Profile {
        // Get obj, then display name string from object
        val obj = jv.obj ?: throw KlaxonException("Null profile")
        val display_name = obj["display_name"] as? String ?: throw KlaxonException("No display_name in profile")


        // Get the map of images
        val images = mutableMapOf<String, String>()
        obj.filterKeys { it.startsWith("image_") }
                .forEach { key, value ->
                    images[key.removePrefix("image_")] =
                            value as? String ?: throw KlaxonException("$key did contain a string") }
        if (images.isEmpty()) {
            throw KlaxonException("No images found for profile")
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

        // Get the largest number
        return images
                .filterKeys { when(it.toIntOrNull()){
                    null -> false
                    else -> true
                }}
                .maxBy { it.key.toInt() }!!.value
    }
}

fun main(args: Array<String>) {
    val f = User::class.java.getResource("/test.json")
    val t = f.readText()
    println(t)
    val u = Klaxon()
            .converter(ProfileConverter)
            .parse<User>(t)!!
    println(u.profile.getLargestImage())
}