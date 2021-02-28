package json.slack.message

import json.reifiedAdapter
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import utils.TestUtils

internal class BotAdminMessageTest : TestUtils {
  private val adapter = MessageTestUtils.moshi.reifiedAdapter<BaseMessage>()

  @Test
  fun goodMessageDeserialisation() {
    val input = readResource("bot-admin-message.json")
    val parsed = adapter.fromJson(input)!! as BotAdminMessage

    assertThat(parsed.ts).isEqualTo("1565580315.003300")
    assertThat(parsed.subtype).isEqualTo(BotAdminEvent.BOT_ADD)
    assertThat(parsed.user).isEqualTo("U02GZ1BHG")
    assertThat(parsed.text).isEqualTo("added an integration to this channel: <https://team.slack.com/services/B0D7B1DSS|incoming-webhook>")
  }
}