package retrofit

import okhttp3.HttpUrl.Companion.toHttpUrl
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class UrlTest {
    private val testApi = DaggerRetrofitTestComponent.builder()
            .url("https://a-fake-url.com/api/".toHttpUrl())
            .build()
            .getTestApi()

    @Test
    fun checkUrlConstruction() {
        val noParamUrl = testApi.listFiles()
                .request()
                .url
                .toString()
        assertThat(noParamUrl).endsWith("list?count=100")

        val paramUrl = testApi.listFiles(user = "A &User")
                .request()
                .url
                .toString()
        assertThat(paramUrl).endsWith("list?count=100&fake_user=A%20%26User")
    }
}