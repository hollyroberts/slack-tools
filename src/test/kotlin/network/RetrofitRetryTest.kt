package network

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import network.SlackTier.TIER_4
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import utils.ThreadSleeper

class RetrofitRetryTest {
  private val threadSleeper: ThreadSleeper = mockk()

  private fun getApi(server: MockWebServer) = DaggerRetrofitTestComponent.builder()
      .url(server.url(""))
      .threadSleeper(threadSleeper)
      .build()
      .getTestApi()

  @BeforeEach
  fun setupClass() {
    every { threadSleeper.sleep(TIER_4.waitTimeMillis) } returns Unit
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

      verify(exactly = 2) { threadSleeper.sleep(TIER_4.waitTimeMillis) }
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