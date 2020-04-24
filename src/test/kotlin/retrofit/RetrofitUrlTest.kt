package retrofit

import okhttp3.HttpUrl.Companion.toHttpUrl
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class RetrofitUrlTest {
    private val retrofit = DaggerRetrofitTestComponent.builder()
            .url("https://a-fake-url.com/api/".toHttpUrl())
            .build()
            .getRetrofit()

    @Test
    fun checkUrlConstruction() {
        val noParamUrl = retrofit.listFiles()
                .request()
                .url
                .toString()
        assertThat(noParamUrl).endsWith("list?count=100")

        val paramUrl = retrofit.listFiles(user = "A &User")
                .request()
                .url
                .toString()
        assertThat(paramUrl).endsWith("list?count=100&fake_user=A%20%26User")
    }
}