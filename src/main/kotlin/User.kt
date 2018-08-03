import com.google.gson.Gson

data class User(
        // Strings and profile
        val id: String,
        val team_id: String,
        val name: String,
        val profile: Profile,

        // Booleans
        val is_bot: Boolean
)

data class Profile(
        val display_name: String
)

fun main(args: Array<String>) {
    val f = User::class.java.getResource("/test.json")
    val t = f.readText()
    print(t)
    val g = Gson()
    val u = g.fromJson(t, User::class.java)
    println(u)
}