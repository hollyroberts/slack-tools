package retrofit

import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class RetryTest {
    private fun getApi(server: MockWebServer) = DaggerRetrofitTestComponent.builder()
            .url(server.url(""))
            .build()
            .getTestApi()

    @Test
    fun successfulCall() {
        val server = MockWebServer()
        server.enqueue(MockResponse().setBody("""
                    ["test1", "test2"]
                    """.trimIndent())
        )

        server.runServer {
            val testApi = getApi(server)
            val response = testApi.listFiles()
                    .execute()

            assertThat(response.isSuccessful).isTrue()
            assertThat(response.body())
                    .isNotNull()
                    .containsExactly("test1", "test2")
        }
    }

    @Test
    fun noRetryAnnotation() {
        val server = MockWebServer()
        server.enqueue(MockResponse().setResponseCode(429))

        server.runServer {
            val testApi = getApi(server)
            val response = testApi.retryTest()

            assertThat(response.result).isEqualTo("success1")
        }
    }

    @Test
    fun singleRetry() {
        val server = MockWebServer()
        server.enqueue(MockResponse().setResponseCode(429))

        server.runServer {
            val testApi = getApi(server)
            val response = testApi.retryTest()

            assertThat(response.result).isEqualTo("success")
        }
    }

}    private inline fun MockWebServer.runServer(function: () -> Unit) {
        this.start()
        try {
            function.invoke()
        } finally {
            this.shutdown()
        }
    }