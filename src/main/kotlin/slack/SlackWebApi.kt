package slack

import slackjson.CompleteFile
import utils.*
import java.nio.file.Path

class SlackWebApi(token: String, settings: Settings) : SlackData(settings) {
    val api = WebApi(token)
    
    override val conversations = api.getConversations()
    override val users = api.getUsers()
}