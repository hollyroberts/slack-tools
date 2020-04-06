package slackjson

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.entry
import org.junit.jupiter.api.Test

@Suppress("UsePropertyAccessSyntax")
internal class ParsedFileTest : TestUtils {
    private val moshi = DaggerTestMoshiComponent.create().getMoshi()
    private val adapter = moshi.adapter(ParsedFile::class.java)

    @Suppress("SpellCheckingInspection")
    @Test
    fun correctObject() {
        val input = readResource("parsedfile-basic.json")
        val parsed = adapter.fromJson(input)!!

        assertThat(parsed.id).isEqualTo("F0S43PZDF")
        assertThat(parsed.user).isEqualTo("U061F7AUR")
        assertThat(parsed.title).isEqualTo("tedair.gif")
        assertThat(parsed.mode).isEqualTo("hosted")
        assertThat(parsed.filetype).isEqualTo("gif")
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

        // TODO assert inferred location
    }
}