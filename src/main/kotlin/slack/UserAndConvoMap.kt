package slack

import utils.WebApi
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserAndConvoMap @Inject constructor(val api: WebApi) : SlackData() {
    override val conversations = api.getConversations()
    override val users = api.getUsers()
}