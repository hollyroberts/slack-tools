package json.slack.message

import com.squareup.moshi.JsonDataException
import io.mockk.mockk
import json.DaggerTestComponent
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import slack.Settings
import utils.TestUtils

internal class BaseMessageDeserialisationTest : TestUtils {
    private val moshi = DaggerTestComponent.builder()
            .settings(Settings())
            .slackData(mockk())
            .api(mockk())
            .build()
            .getMoshi()
    private val adapter = moshi.adapter(BaseMessage::class.java)

    @Test
    fun textMessageDeserialisation() {
        val input = readResource("basic-message.json")
        val parsed = adapter.fromJson(input)!! as TextMessage

        assertThat(parsed.ts).isEqualTo("1355517523.000005")
        assertThat(parsed.text).isEqualTo("Hello world")
    }

    @Test
    fun channelJoin() {
        val input = readResource("channel-join.json")
        val parsed = adapter.fromJson(input)!! as ChannelMessage

        assertThat(parsed.subtype).isEqualTo(ChannelType.CHANNEL_JOIN)
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