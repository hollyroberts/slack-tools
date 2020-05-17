package utils

import org.assertj.core.api.SoftAssertions
import org.junit.jupiter.api.Test


internal class UtilsKtTest {
    @Test
    fun formatSizeTest() {
        SoftAssertions.assertSoftly { softly ->
            // Main assertions
            softly.assertThat(formatSize(0, sigFigures = 3)).isEqualTo("0 B")
            softly.assertThat(formatSize(1, sigFigures = 3)).isEqualTo("1 B")
            softly.assertThat(formatSize(500, sigFigures = 3)).isEqualTo("500 B")
            softly.assertThat(formatSize(999, sigFigures = 3)).isEqualTo("999 B")
            softly.assertThat(formatSize(1000, sigFigures = 3)).isEqualTo("1000 B")
            softly.assertThat(formatSize(1023, sigFigures = 3)).isEqualTo("1023 B")
            softly.assertThat(formatSize(1024, sigFigures = 3)).isEqualTo("1.0 KiB")
            softly.assertThat(formatSize(1034, sigFigures = 3)).isEqualTo("1.0 KiB")
            softly.assertThat(formatSize(1035, sigFigures = 3)).isEqualTo("1.01 KiB")
            softly.assertThat(formatSize(1200, sigFigures = 3)).isEqualTo("1.17 KiB")
            softly.assertThat(formatSize(2000, sigFigures = 3)).isEqualTo("1.95 KiB")
            softly.assertThat(formatSize(99 * 1024 + 304, sigFigures = 3)).isEqualTo("99.2 KiB")
            softly.assertThat(formatSize(100 * 1024 + 50, sigFigures = 3)).isEqualTo("100 KiB")
            softly.assertThat(formatSize((1024 * 1024) - 1, sigFigures = 3)).isEqualTo("1023 KiB")
            softly.assertThat(formatSize(1024 * 1024, sigFigures = 3)).isEqualTo("1.0 MiB")
            softly.assertThat(formatSize((1024L * 1024 * 1024 * 1024 * 1024 * 1024) - 1, sigFigures = 3)).isEqualTo("1023 PiB")
            softly.assertThat(formatSize(1024L * 1024 * 1024 * 1024 * 1024 * 1024, sigFigures = 3)).isEqualTo("1.0 EiB")

            // With some other precisions
            softly.assertThat(formatSize(0, sigFigures = 0)).isEqualTo("0 B")
            softly.assertThat(formatSize(1000 * 1024, sigFigures = 0)).isEqualTo("1000 KiB")
            softly.assertThat(formatSize(999, sigFigures = 2)).isEqualTo("999 B")
            softly.assertThat(formatSize(99 * 1024 + 304, sigFigures = 2)).isEqualTo("99 KiB")
            softly.assertThat(formatSize(99 * 1024 + 304, sigFigures = 4)).isEqualTo("99.29 KiB")
        }
    }
}