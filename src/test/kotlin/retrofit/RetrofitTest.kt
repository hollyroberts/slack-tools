package retrofit

import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class RetrofitTest {
    private val retrofit = DaggerRetrofitTestComponent.builder()
            .settings(mockk())
            .slackData(mockk())
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
        assertThat(paramUrl).endsWith("list?count=100&user=A%20%26User")
    }
}