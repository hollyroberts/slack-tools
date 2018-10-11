package slack

import utils.Api

class SlackData(val token: String) {
    val users by lazy { Api.getUsers() }
}