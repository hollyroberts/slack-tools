package retrofit

import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockkObject
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle

@TestInstance(Lifecycle.PER_CLASS)
class RetryTest {
    private fun getApi(server: MockWebServer) = DaggerRetrofitTestComponent.builder()
            .url(server.url(""))
            .build()
            .getTestApi()

    @BeforeAll
    fun setupClass() {
        mockkObject(SlackTier.TIER_4)
        every { SlackTier.TIER_4.waitTimeMillis } returns 50
    }

    @AfterAll
    fun clearClass() {
        clearMocks(SlackTier.TIER_4)
    }

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
    fun maxRetries() {
        val server = MockWebServer()
        server.enqueue(MockResponse().setResponseCode(429))
        server.enqueue(MockResponse().setResponseCode(429))
        server.enqueue(MockResponse().setBody("[\"success\"]"))

        server.runServer {
            val testApi = getApi(server)
            val response = testApi.retryTest()

            assertThat(response.result)
                    .containsExactly("success")

            assertThat(server.requestCount).isEqualTo(3)
        }
    }

    @Test
    fun retriesExceeded() {
        println(SlackTier.TIER_4.waitTimeMillis)

        val server = MockWebServer()
        server.enqueue(MockResponse().setResponseCode(429))
        server.enqueue(MockResponse().setResponseCode(429))
        server.enqueue(MockResponse().setResponseCode(429))
        server.enqueue(MockResponse().setBody("[\"success\"]"))

        server.runServer {
            val testApi = getApi(server)
            assertThatThrownBy { testApi.retryTest() }
                    .isInstanceOf(RuntimeException::class.java)
                    .hasMessageContaining("Call to /retry.test failed after 3 attempts")
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