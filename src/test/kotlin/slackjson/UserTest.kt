package slackjson

import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import slack.Settings
import utils.TestUtils

internal class UserTest : TestUtils {
    private val moshi = DaggerTestComponent.builder()
            .settings(Settings())
            .slackData(mockk())
            .api(mockk())
            .build()
            .getMoshi()
    private val adapter = moshi.adapter(User::class.java)

    @Test
    fun userDeserialisation() {
        val input = readResource("user-good.json")
        val parsed = adapter.fromJson(input)!!

        assertThat(parsed.id).isEqualTo("W012A3CDE")
        assertThat(parsed.teamId).isEqualTo("T012AB3C4")
        assertThat(parsed.deleted).isFalse()

        assertThat(parsed.username()).isEqualTo("spengler")
        assertThat(parsed.displayname()).isEqualTo("spengler")
        assertThat(parsed.isBot()).isFalse()

        assertThat(parsed.profile.images).containsExactlyInAnyOrderEntriesOf(mapOf(
                "24" to "https://.../avatar/e3b51ca72dee4ef87916ae2b9240df50.jpg",
                "32" to "https://.../avatar/e3b51ca72dee4ef87916ae2b9240df50.jpg",
                "48" to "https://.../avatar/e3b51ca72dee4ef87916ae2b9240df50.jpg",
                "72" to "https://.../avatar/e3b51ca72dee4ef87916ae2b9240df50.jpg",
                "192" to "https://.../avatar/e3b51ca72dee4ef87916ae2b9240df50.jpg",
                "512" to "https://.../avatar/e3b51ca72dee4ef87916ae2b9240df50.jpg",
                "original" to "https://.../avatar/e3b51ca72dee4ef87916ae2b9240df50-large.jpg"
        ))
    }

    @Test
    fun getLargestImageFullSet() {
        val input = readResource("user-good.json")
        val parsed = adapter.fromJson(input)!!

        assertThat(parsed.profile.getLargestImage()).isEqualTo("https://.../avatar/e3b51ca72dee4ef87916ae2b9240df50-large.jpg")
    }

    @Test
    fun getLargestImageSomeInvalidImages() {
        val input = readResource("user-invalid-images.json")
        val parsed = adapter.fromJson(input)!!

        assertThat(parsed.profile.getLargestImage()).isEqualTo("https://.../avatar/e3b51ca72dee4ef87916ae2b9240df51.jpg")
    }

    @Test
    fun getLargestImageOnlyIntegers() {
        val input = readResource("user-only-int-images.json")
        val parsed = adapter.fromJson(input)!!

        assertThat(parsed.profile.getLargestImage()).isEqualTo("https://.../avatar/e3b51ca72dee4ef87916ae2b9240df51.jpg")
    }
}