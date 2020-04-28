package slackjson

import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.entry
import org.junit.jupiter.api.Test
import slack.Settings
import slack.SlackData
import utils.TestUtils
import java.time.ZoneId

@Suppress("UsePropertyAccessSyntax", "SpellCheckingInspection")
internal class ParsedFileTest : TestUtils {
    private val settings: Settings = mockk()
    private val slackData: SlackData = mockk()

    private val moshi = DaggerTestComponent.builder()
            .settings(settings)
            .slackData(slackData)
            .build()
            .getMoshi()
    private val adapter = moshi.adapter(ParsedFile::class.java)

    @Test
    fun correctObject() {
        val input = readResource("parsedfile-basic.json")
        val parsed = adapter.fromJson(input)!!

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
                .satisfies {
                    assertThat(it!!.firstSeen).contains(entry("C0T8SE4AU", 1531763348.000001))
                }
    }

    @Test
    fun formattedName() {
        val input = readResource("parsedfile-basic.json")
        val parsed = adapter.fromJson(input)!!

        every { slackData.userDisplayname("U061F7AUR") } returns "test_user"
        every { settings.useDisplayNamesForFiles } returns true
        every { settings.outTz } returns ZoneId.of("UTC")

        assertThat(parsed.formattedDownloadName(SlackFile.FormattingType.STANDARD)).isEqualTo("[2018-07-16 - 17;49] - test_user - tedair.gif")
    }

    // TODO Test inferred location (using mocks!)
}