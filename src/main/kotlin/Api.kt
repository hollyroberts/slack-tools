import com.squareup.moshi.Moshi

object Api {
    // URLs
    private const val URL_USERS_LIST = "https://slack.com/api/users.list"
    private const val URL_FILES_LIST = "https://slack.com/api/files.list"

    // Limits
    private const val USERS_LIST_LIMIT = 100
    private const val FILE_LIST_LIMIT = 50

    // Rate limit times to wait (in ms)
    private const val RETRY_TIER_1 = 60 * 1000
    private const val RETRY_TIER_2 = 3 * 1000
    private const val RETRY_TIER_3 = 1 * 1000
    private const val RETRY_TIER_4 = 0.5 * 1000

    private val moshi = Moshi.Builder()
            .add(ProfileJsonAdapter)
            .build()!!

    fun getFiles(startTime: Int = 0, endTime: Int? = null) : List<File> {
        val params = mutableMapOf(
                "page" to "1",
                "count" to FILE_LIST_LIMIT.toString(),
                "start_ts" to startTime.toString(),
                "end_ts" to (endTime?.toString() ?: "now")
        )
        val adapter = moshi.adapter(FileList::class.java)!!

        // Get results
        val files = mutableListOf<File>()
        do {
            val response = (Http.get(URL_FILES_LIST, adapter, params) as Result.Success).value!!
            files.addAll(response.files)

            Log.info("Retrieved ${files.size}/${response.paging.total} files (page ${response.paging.page}/${response.paging.pages})")
        } while (response.updatePageParams(params))
        
        return files
    }

    /**
     * Retrieves full list of users using Slack API
     * @return map of userid to user object
     */
    fun getUsers() : Map<String, User> {
        val userMap = mutableMapOf<String, User>()
        val params = mutableMapOf(
                "limit" to USERS_LIST_LIMIT.toString(),
                "cursor" to "")
        val adapter = moshi.adapter(UserList::class.java)!!

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