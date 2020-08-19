package json.slack.message

import com.squareup.moshi.JsonDataException
import json.reifiedAdapter
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import utils.TestUtils

internal class MessageDeserialisationTest : TestUtils {
    private val adapter = MessageTestUtils.moshi.reifiedAdapter<BaseMessage>()

    @Test
    fun textMessageDeserialisation() {
        val input = readResource("basic-message.json")
        val parsed = adapter.fromJson(input)!! as TextMessage

        assertThat(parsed.ts).isEqualTo("1355517523.000005")
        assertThat(parsed.text).isEqualTo("Hello world")
    }

    @Test
    fun channelSubtype() {
        val input = readResource("channel-join.json")
        val parsed = adapter.fromJson(input)!! as ChannelMessage

        assertThat(parsed.subtype).isEqualTo(ChannelEvent.CHANNEL_JOIN)
    }

    @Test
    @Suppress("SpellCheckingInspection")
    fun invalidType() {
        val input = readResource("invalid-type.json")
        assertThatThrownBy { adapter.fromJson(input) }
                .isInstanceOf(JsonDataException::class.java)
                .hasMessageContaining("Message type was not 'message', but was 'messagee'")
    }

    @Test
    fun unknownType() {
        val input = readResource("unknown-subtype.json")
        assertThat(adapter.fromJson(input)).isNull()
    }
}