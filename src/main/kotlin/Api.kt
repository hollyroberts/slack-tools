import com.squareup.moshi.Moshi

object Api {
    // URLs
    private const val URL_USERS_LIST = "https://slack.com/api/users.list"

    // Limits
    private const val USERS_LIST_LIMIT = 50

    // Rate limit times to wait (in ms)
    private const val RETRY_TIER_1 = 60 * 1000
    private const val RETRY_TIER_2 = 3 * 1000
    private const val RETRY_TIER_3 = 1 * 1000
    private const val RETRY_TIER_4 = 0.5 * 1000

    private val moshi = Moshi.Builder()
            .add(ProfileJsonAdapter)
            .build()!!
    private val adapter = moshi.adapter(UserList::class.java)!!

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
            // Get converted response
            val response = (Http.get(URL_USERS_LIST, adapter, params) as Result.Success).value!!

            // Add entries to map
            response.members.forEach {
                userMap[it.id] = it
            }

            // Check cursor
            if (!response.moreEntries()) {
                break
            } else {
                params["cursor"] = response.nextCursor()!!
            }
        } while (true)

        return userMap
    }
}