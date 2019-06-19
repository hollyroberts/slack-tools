package slack

import slackjson.CompleteFile
import utils.*
import java.nio.file.Path

class SlackWebApi(token: String, settings: Settings) : SlackData(settings) {
    private val LOCATION_INTERVAL = 3000

    val api = WebApi(token)
    
    override val conversations = api.getConversations()
    override val users = api.getUsers()
}