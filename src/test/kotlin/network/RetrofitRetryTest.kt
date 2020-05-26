package network

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
import java.time.Duration

@TestInstance(Lifecycle.PER_CLASS)
class RetrofitRetryTest {
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
            val response = testApi.pathTest()
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

            assertThat(response).containsExactly("success")
            assertThat(server.requestCount).isEqualTo(3)
        }
    }

    @Test
    fun retriesExceeded() {
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

    @Test
    fun retryInterval() {
        val server = MockWebServer()
        val dispatcher = TimeRecorderDispatcher()
        dispatcher.addResponse(MockResponse().setResponseCode(429))
        dispatcher.addResponse(MockResponse().setResponseCode(429))
        dispatcher.addResponse(MockResponse().setBody("[\"success\"]"))
        server.dispatcher = dispatcher

        server.runServer {
            val testApi = getApi(server)
            val response = testApi.retryTest()

            assertThat(response).containsExactly("success")

            assertThat(server.requestCount).isEqualTo(3)

            val timeDiffs = List(dispatcher.dispatchTimes.size - 1) {
                Duration.between(
                        dispatcher.dispatchTimes[it],
                        dispatcher.dispatchTimes[it + 1]
                ).toMillisPart()
            }

            assertThat(timeDiffs).allSatisfy {
                assertThat(it).isBetween(50, 60)
            }
        }
    }

    @Test
    fun requireAnnotationTest() {
        val server = MockWebServer()
        server.runServer {
            val testApi = getApi(server)

            assertThatThrownBy { testApi.annotationTest() }
                    .isInstanceOf(IllegalArgumentException::class.java)
                    .hasMessage("Unable to create call adapter for java.util.List<java.lang.String>" +
                            "\n    for method RetrofitTestApi.annotationTest")
        }
    }
}