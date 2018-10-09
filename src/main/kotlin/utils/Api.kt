package utils

import com.squareup.moshi.Moshi
import slackjson.*

object Api {
    // URLs
    private const val URL_CONVO_LIST = "https://slack.com/api/conversations.list"
    private const val URL_FILES_INFO = "https://slack.com/api/files.info"
    private const val URL_FILES_LIST = "https://slack.com/api/files.list"
    private const val URL_USERS_LIST = "https://slack.com/api/users.list"

    // Limits
    private const val CONVO_LIST_LIMIT = 100
    private const val FILE_LIST_LIMIT = 100
    private const val USERS_LIST_LIMIT = 100

    // Rate limit times to wait (in s)
    private const val RETRY_TIER_1 = 60
    private const val RETRY_TIER_2 = 3
    private const val RETRY_TIER_3 = 1
    private const val RETRY_TIER_4 = 1

    val moshi = Moshi.Builder()
            .add(ProfileJsonAdapter)
            .add(ShareJsonAdapter)
            .build()!!

    /**
     * Returns a list of conversations (channels, groups, ims)
     */
    fun getConversations() : List<Conversation> {
        val convos = mutableListOf<Conversation>()
        val params = mutableMapOf(
                "limit" to CONVO_LIST_LIMIT.toString(),
                "types" to "public_channel, private_channel, im",
                "cursor" to "")
        val adapter = moshi.adapter(ConversationListResponse::class.java)!!

        Log.medium("Retrieving conversations (channels)")
        do {
            // Get converted response
            val response = (Http.get(URL_CONVO_LIST, adapter, params, RETRY_TIER_2) as Result.Success).value!!

            // Add entries to map
            convos.addAll(response.channels)
            Log.debugHigh("Retrieved ${response.channels.size} conversations")

            // Check cursor
            if (!response.moreEntries()) {
                break
            } else {
                params["cursor"] = response.nextCursor()!!
            }
        } while (true)

        return convos.toList()
    }

    /**
     * Returns a list of parsed files (which may be incomplete) in a time range
     */
    fun getFiles(startTime: Int = 0, endTime: Int? = null) : List<ParsedFile> {
        val params = mutableMapOf(
                "page" to "1",
                "count" to FILE_LIST_LIMIT.toString(),
                "start_ts" to startTime.toString(),
                "end_ts" to (endTime?.toString() ?: "now")
        )
        val adapter = moshi.adapter(FileListResponse::class.java)!!


        // Get results
        Log.low("Retrieving list of files")
        val files = mutableListOf<ParsedFile>()
        do {
            val response = (Http.get(URL_FILES_LIST, adapter, params, RETRY_TIER_3) as Result.Success).value!!
            files.addAll(response.files)

            Log.medium("Retrieved ${files.size}/${response.paging.total} files (page ${response.paging.page}/${response.paging.pages})")
        } while (response.updatePageParams(params))

        Log.low("Retrieved ${files.size} files")
        return files
    }

    /**
     * Returns a file from id (files.info)
     */
    fun getFile(fileId: String) : ParsedFile {
        val params = mapOf("file" to fileId)
        val adapter = moshi.adapter(FileResponse::class.java)!!
        val response = (Http.get(URL_FILES_INFO, adapter, params, RETRY_TIER_4) as Result.Success).value!!

        return response.file
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
        val adapter = moshi.adapter(UserListResponse::class.java)!!

        Log.medium("Retrieving user results")
        do {
            // Get converted response
            val response = (Http.get(URL_USERS_LIST, adapter, params, RETRY_TIER_2) as Result.Success).value!!

            // Add entries to map
            response.members.forEach {
                userMap[it.id] = it
            }
            Log.debugHigh("Retrieved ${userMap.size} user results")

            // Check cursor
            if (!response.moreEntries()) {
                break
            } else {
                params["cursor"] = response.nextCursor()!!
            }
        } while (true)

        Log.medium("Finished retrieving user results (${userMap.size} found)")
        return userMap
    }
}