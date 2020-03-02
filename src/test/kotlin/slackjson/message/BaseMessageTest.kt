package slackjson.message

import com.squareup.moshi.Moshi
import org.junit.jupiter.api.Test
import org.assertj.core.api.Assertions.assertThat

internal class BaseMessageTest {
    private val BASIC_MESSAGE = """
    {
    "type": "message",
    "channel": "C2147483705",
    "user": "U2147483697",
    "text": "Hello world",
    "ts": "1355517523.000005"
    }"""

    @Test
    fun basicMessageSerialisation() {
        val adapter = Moshi.Builder()
                .add(BaseMessageCustomAdapter)
                .build()
                .adapter(BaseMessage::class.java)
        val parsed = adapter.fromJson(BASIC_MESSAGE)!! as TextMessage

        assertThat(parsed.ts).isEqualTo("1355517523.000005")
        assertThat(parsed.text).isEqualTo("Hello world")
    }
}