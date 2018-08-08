package slack

import slackjson.User

class SlackUsers private constructor(val users: Map<String, User>) {
    fun downloadAvatars() {
        TODO("Not implemented")
    }

    // Factory methods
    companion object {
        fun create(userMap: Map<String, User>) : SlackUsers {

            return SlackUsers(userMap)
        }
    }
}