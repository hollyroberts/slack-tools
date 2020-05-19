package slack

import retrofit.SlackApi
import utils.WebApi
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserAndConvoMap @Inject constructor(
        val oldApi: WebApi,
        private val api: SlackApi
) : SlackData() {
    override val conversations = oldApi.getConversations()
    override val users = api.listUsers()
}