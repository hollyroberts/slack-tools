package slackjson

import com.squareup.moshi.JsonDataException
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

internal class ConversationTest : TestUtils {
    private val moshi = DaggerTestMoshiComponent.create().getMoshi()
    private val adapter = moshi.adapter(Conversation::class.java)

    @Test
    fun correctObject() {
        val input = readResource("conversation-basic.json")
        val parsed = adapter.fromJson(input)!!

        assertThat(parsed.id).isEqualTo("C012AB3CD")
        assertThat(parsed.isChannel).isEqualTo(true)
        assertThat(parsed.isGroup).isEqualTo(false)
        assertThat(parsed.isIm).isEqualTo(false)
        assertThat(parsed.name).isEqualTo("general")
        assertThat(parsed.user).isNull()
    }

    @Test
    fun incorrectConversationType() {
        val input = readResource("conversation-incorrect-type.json")
        assertThatThrownBy { adapter.fromJson(input) }
                .hasRootCauseInstanceOf(JsonDataException::class.java)
                .hasRootCauseMessage("Conversation C012AB3CD (general) has more than 1 channel type")
    }

    @Test
    fun correctInstantMessage() {
        val input = readResource("conversation-im.json")
        val parsed = adapter.fromJson(input)!!

        assertThat(parsed.user).isEqualTo("U0BS9U4SV")
    }

    @Test
    fun incorrectInstantMessage() {
        val input = readResource("conversation-incorrect-im.json")

        assertThatThrownBy { adapter.fromJson(input)!! }
                .hasRootCauseInstanceOf(JsonDataException::class.java)
                .hasRootCauseMessage("Conversation D0C0F7S8Y is a dm, but does not contain a user field")
    }
}