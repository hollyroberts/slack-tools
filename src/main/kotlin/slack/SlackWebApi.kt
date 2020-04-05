package slack

import utils.WebApi
import javax.inject.Inject

class SlackWebApi @Inject constructor(val api: WebApi, settings: Settings) : SlackData(settings) {
    override val conversations = api.getConversations()
    override val users = api.getUsers()
}