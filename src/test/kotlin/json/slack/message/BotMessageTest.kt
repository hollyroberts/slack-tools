package json.slack.message

import json.reifiedAdapter
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import utils.TestUtils

internal class BotMessageTest : TestUtils {
    private val adapter = MessageTestUtils.moshi.reifiedAdapter<BaseMessage>()

    @Test
    fun goodMessageDeserialisation() {
        val input = readResource("bot-message.json")
        val parsed = adapter.fromJson(input)!! as BotMessage

        assertThat(parsed.ts).isEqualTo("1597825947.041600")
        assertThat(parsed.subtype).isEqualTo(OtherEvent.BOT_MESSAGE)
        assertThat(parsed.username).isEqualTo("The Bot Name")
        assertThat(parsed.text).isEqualTo("Some sort of bot message")
    }
}