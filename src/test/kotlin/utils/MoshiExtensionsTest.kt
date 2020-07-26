package utils

import com.squareup.moshi.Moshi
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class MoshiExtensionsTest : TestUtils {
    @ExperimentalStdlibApi
    @Test
    fun moshiExtensionAdapter() {
        val input = readResource("nested_data.json")
        val moshi = Moshi.Builder().build()

        val adapter = moshi.reifiedAdapter<Map<String, List<Int>>>()
        val parsed = adapter.fromJson(input)!!

        assertThat(parsed).containsExactlyInAnyOrderEntriesOf(mapOf(
                "Hello" to listOf(3, 2, 1),
                "World" to listOf(4, 5, 6)
        ))
    }
}