package slack

import utils.WebApi
import javax.inject.Inject

class SlackWebApi @Inject constructor(val api: WebApi) : SlackData() {
    override val conversations = api.getConversations()
    override val users = api.getUsers()
}