package slack

import utils.Api
import utils.Http

class SlackDataFromApi(token: String, settings: Settings) : SlackData(settings) {
    override val conversations by lazy { Api.getConversations() }
    override val filesParsed by lazy { Api.getFiles() }
    override val users by lazy { Api.getUsers() }

    init {
        Http.token = token
    }
}

data class Settings(
        val inferFileLocation: Boolean = true,
        val ignoreDownloadedFiles: Boolean = true
)