package json

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonDataException
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import json.slack.file.ParsedFile
import json.slack.file.SlackFile
import network.SlackApi
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.Test
import slack.Settings
import slack.SlackData
import utils.TestUtils
import java.math.BigDecimal
import java.time.ZoneId
import java.util.function.Consumer

@Suppress("UsePropertyAccessSyntax", "SpellCheckingInspection")
internal class ParsedFileTest : TestUtils {
  private fun getAdapter(settings: Settings = mockk(),
                         slackData: SlackData = mockk(),
                         api: SlackApi = mockk()
  ): JsonAdapter<ParsedFile> {
    val moshi = DaggerTestComponent.builder()
        .settings(settings)
        .slackData(slackData)
        .api(api)
        .build()
        .getMoshi()
    return moshi.adapter(ParsedFile::class.java)!!
  }

  @Test
  fun correctObject() {
    val input = readResource("parsedfile-basic.json")
    val parsed = getAdapter().fromJson(input)!!

    assertThat(parsed.id).isEqualTo("F0S43PZDF")
    assertThat(parsed.user).isEqualTo("U061F7AUR")
    assertThat(parsed.title).isEqualTo("tedair.gif")
    assertThat(parsed.mode).isEqualTo("hosted")
    assertThat(parsed.filetype).isEqualTo("gif")
    assertThat(parsed.size).isEqualTo(137531)
    assertThat(parsed.timestamp).isEqualTo(1531763342)
    assertThat(parsed.urlPrivate).isEqualTo("https://.../tedair.gif")
    assertThat(parsed.urlPrivateDownload).isEqualTo("https://.../tedair.gif")

    assertThat(parsed.channels).containsExactly("C0T8SE4AU")
    assertThat(parsed.groups).isEmpty()
    assertThat(parsed.ims).isEmpty()

    assertThat(parsed.shares)
        .isNotNull()
        .satisfies(Consumer {
          assertThat(it!!.firstSeen).contains(entry("C0T8SE4AU", BigDecimal("1531763348.000001")))
        })
  }

  @Test
  fun mustBePrivateOrPublic() {
    val input = readResource("parsedfile-incorrect-channel-type.json")
    assertThatThrownBy { getAdapter().fromJson(input) }
        .hasRootCauseInstanceOf(JsonDataException::class.java)
        .hasRootCauseMessage("File share data was not public or private (was privatte)")
  }

  @Test
  fun formattedName() {
    val settings: Settings = mockk()
    val slackData: SlackData = mockk()
    val input = readResource("parsedfile-basic.json")
    val parsed = getAdapter(settings = settings, slackData = slackData).fromJson(input)!!

    every { slackData.userDisplayname("U061F7AUR") } returns "test_user"
    every { settings.useDisplayNamesForFiles } returns true
    every { settings.outTz } returns ZoneId.of("UTC")

    assertThat(parsed.formattedDownloadName(SlackFile.FormattingType.STANDARD)).isEqualTo("[2018-07-16 - 17;49] - test_user - tedair.gif")
  }

  @Test
  fun inferLocationFromTimestamp() {
    val input = readResource("parsedfile-basic.json")
    val parsed = getAdapter().fromJson(input)!!

    parsed.addLocationTimestamp("alpha", BigDecimal("123.45"))
    parsed.addLocationTimestamp("bravo", BigDecimal("123.123"))
    parsed.addLocationTimestamp("alpha", BigDecimal("123.321"))

    val completeFile = parsed.toCompleteFileFromTimestamps()
    assertThat(completeFile.uploadLoc).isEqualTo("bravo")
  }

  @Test
  fun inferLocationNoTimestampsProvided() {
    val input = readResource("parsedfile-basic.json")
    val parsed = getAdapter().fromJson(input)!!

    val completeFile = parsed.toCompleteFileFromTimestamps()
    assertThat(completeFile.uploadLoc).isNull()
  }

  @Test
  fun inferLocationFromSharesSingleLocation() {
    val input = readResource("parsedfile-basic.json")
    val parsed = getAdapter().fromJson(input)!!

    val completeFile = parsed.toCompleteFileByInference()
    assertThat(completeFile.uploadLoc).isEqualTo("C0T8SE4AU")
  }

  @Test
  fun inferLocationFromChannel() {
    val input = readResource("parsedfile-singleloc.json")
    val parsed = getAdapter().fromJson(input)!!

    val completeFile = parsed.toCompleteFileByInference()
    assertThat(completeFile.uploadLoc).isEqualTo("D0T8SE4AU")
  }

  @Test
  fun inferLocationFromApiAfterMultipleChannels() {
    val api: SlackApi = mockk()
    val input = readResource("parsedfile-multiloc-noshares.json")
    val parsed = getAdapter(api = api).fromJson(input)!!

    val parsedShares = readResource("parsedfile-multiloc-shares.json")
    every { api.listFileInfo("F123") } returns getAdapter().fromJson(parsedShares)!!

    val completeFile = parsed.toCompleteFileByInference()
    verify { api.listFileInfo("F123") }
    assertThat(completeFile.uploadLoc).isEqualTo("DAKJT8SE4AU")
  }
}