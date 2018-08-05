import com.beust.klaxon.Klaxon

object Api {
    // URLs
    private const val URL_USERS_LIST = "https://slack.com/api/users.list"

    // Limits
    private const val USERS_LIST_LIMIT = 31

    // Rate limit times to wait (in ms)
    private const val RETRY_TIER_1 = 60 * 1000
    private const val RETRY_TIER_2 = 3 * 1000
    private const val RETRY_TIER_3 = 1 * 1000
    private const val RETRY_TIER_4 = 0.5 * 1000

    /**
     * Retrieves full list of users using Slack API
     * @return map of userid to user object
     */
    fun getUsers() : Map<String, User> {
        val userMap = mutableMapOf<String, User>()
        val params = mutableMapOf(
                "limit" to USERS_LIST_LIMIT.toString(),
                "cursor" to "")

        do {
            // Get response (presume success)
            val response = Http.get(URL_USERS_LIST, params) as Result.Success
            val parsedJson = Klaxon()
                    .converter(ProfileConverter)
                    .parseFromJsonObject<UserList>(response.value)!!

            // Add entries to map
            parsedJson.members.forEach {
                userMap[it.id] = it
            }

            // Check cursor
            if (parsedJson.response_metadata.next_cursor.isNullOrEmpty()) {
                break
            } else {
                params["cursor"] = parsedJson.response_metadata.next_cursor!!
            }
        } while (true)

        println(userMap.size)
        return userMap
    }
}