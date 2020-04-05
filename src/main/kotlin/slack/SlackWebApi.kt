package slack

import utils.WebApi
import javax.inject.Inject
import javax.inject.Named

class SlackWebApi @Inject constructor(@Named("token") token: String, settings: Settings) : SlackData(settings) {
    val api = WebApi(token)
    
    override val conversations = api.getConversations()
    override val users = api.getUsers()
}