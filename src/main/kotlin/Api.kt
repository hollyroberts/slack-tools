import com.squareup.moshi.Moshi

object Api {
    // URLs
    private const val URL_USERS_LIST = "https://slack.com/api/users.list"

    // Limits
    private const val USERS_LIST_LIMIT = 10

    // Rate limit times to wait (in ms)
    private const val RETRY_TIER_1 = 60 * 1000
    private const val RETRY_TIER_2 = 3 * 1000
    private const val RETRY_TIER_3 = 1 * 1000
    private const val RETRY_TIER_4 = 0.5 * 1000

    val moshi = Moshi.Builder()
            .add(ProfileJsonAdapter)
            .build()!!
    val adapter = moshi.adapter(UserList::class.java)

    /**
     * Retrieves full list of users using Slack API
     * @return map of userid to user object
     */
    fun getUsers() : Map<String, User> {
        val userMap = mutableMapOf<String, User>()
        val params = mutableMapOf("limit" to USERS_LIST_LIMIT.toString())

        Http.get(URL_USERS_LIST, adapter, params)

        return mapOf()
    }
}