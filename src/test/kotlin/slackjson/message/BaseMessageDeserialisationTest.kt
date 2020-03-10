package slackjson.message

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class BaseMessageDeserialisationTest {
    private val adapter: JsonAdapter<BaseMessage> = Moshi.Builder()
            .add(BaseMessageCustomAdapter)
            .build()
            .adapter(BaseMessage::class.java)

    private val BASIC_MESSAGE = """
    {
    "type": "message",
    "user": "U2147483697",
    "text": "Hello world",
    "ts": "1355517523.000005"
    }"""

    @Test
    fun textMessageSerialisation() {
        val parsed = adapter.fromJson(BASIC_MESSAGE)!! as TextMessage

        assertThat(parsed.ts).isEqualTo("1355517523.000005")
        assertThat(parsed.text).isEqualTo("Hello world")
    }

    @Test
    fun channelJoin() {
        val input = readResource("channel-join.json")
        adapter.fromJson(input)!! as ChannelMessage
    }

    private fun readResource(resource: String) : String {
        val inputStream  = this::class.java.getResourceAsStream(resource)!!
        return inputStream.reader().readText()
    }
}