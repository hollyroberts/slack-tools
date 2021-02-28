package slack

import network.SlackApi
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserAndConvoMap @Inject constructor(
    api: SlackApi
) : SlackData() {
  override val conversations = api.listConversations()
  override val users = api.listUsers()
}