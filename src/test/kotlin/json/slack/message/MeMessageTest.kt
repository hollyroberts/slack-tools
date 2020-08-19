package json.slack.message

import json.reifiedAdapter
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import utils.TestUtils

internal class MeMessageTest : TestUtils {
    private val adapter = MessageTestUtils.moshi.reifiedAdapter<BaseMessage>()

    @Test
    fun textMessageDeserialisation() {
        val input = readResource("me-message.json")
        val parsed = adapter.fromJson(input)!! as MeMessage

        assertThat(parsed.ts).isEqualTo("1355517523.000006")
        assertThat(parsed.user).isEqualTo("U2147483699")
        assertThat(parsed.text).isEqualTo("Hello Me")
    }
}