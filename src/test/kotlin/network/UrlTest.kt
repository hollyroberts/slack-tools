package network

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
        val noParamUrl = testApi.pathTest()
                .request()
                .url
                .toString()
        assertThat(noParamUrl).endsWith("test?count=100")

        val paramUrl = testApi.pathTest(user = "A &User")
                .request()
                .url
                .toString()
        assertThat(paramUrl).endsWith("test?count=100&fake_user=A%20%26User")
    }
}