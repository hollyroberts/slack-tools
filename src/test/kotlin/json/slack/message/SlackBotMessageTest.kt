package json.slack.message

import com.squareup.moshi.JsonDataException
import json.reifiedAdapter
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import utils.TestUtils

internal class SlackBotMessageTest : TestUtils {
    private val adapter = MessageTestUtils.moshi.reifiedAdapter<BaseMessage>()

    @Test
    fun goodMessageDeserialisation() {
        val input = readResource("slackbot-response-message.json")
        val parsed = adapter.fromJson(input)!! as SlackBotMessage

        assertThat(parsed.ts).isEqualTo("1560352867.072221")
        assertThat(parsed.text).isEqualTo("Standup is at 9am daily!")
    }

    @Test
    fun noUserProvided() {
        val input = readResource("slackbot-response-no-user-field.json")
        assertThatThrownBy { adapter.fromJson(input) }
                .isInstanceOf(JsonDataException::class.java)
                .hasMessageContaining("Did not encounter user field for slackbot response message type")
    }

    @Test
    fun wrongUsername() {
        val input = readResource("slackbot-response-bad-user.json")
        assertThatThrownBy { adapter.fromJson(input) }
                .isInstanceOf(JsonDataException::class.java)
                .hasMessageContaining("Slackbot messages must have user type of 'USLACKBOT'")
    }
}