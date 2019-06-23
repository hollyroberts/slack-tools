package slack

import utils.*

class SlackWebApi(token: String, settings: Settings) : SlackData(settings) {
    val api = WebApi(token)
    
    override val conversations = api.getConversations()
    override val users = api.getUsers()
}