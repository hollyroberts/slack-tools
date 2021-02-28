package json.slack.message

import json.reifiedAdapter
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import utils.TestUtils

internal class ReminderAddMessageTest : TestUtils {
  private val adapter = MessageTestUtils.moshi.reifiedAdapter<BaseMessage>()

  @Test
  fun goodMessageDeserialisation() {
    val input = readResource("reminder-add-message.json")
    val parsed = adapter.fromJson(input)!! as ReminderAddMessage

    assertThat(parsed.ts).isEqualTo("1481104005.000012")
    assertThat(parsed.user).isEqualTo("U02JQMMAB")
    assertThat(parsed.text).isEqualTo("set up a reminder to “do the thing” in this channel at 9pm today, Greenwich Mean Time.")
  }
}