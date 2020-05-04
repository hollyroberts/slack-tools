package retrofit

import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import utils.TestUtils

class SlackAdapterTest : TestUtils {
    private fun getApi(server: MockWebServer) = DaggerRetrofitTestComponent.builder()
            .url(server.url(""))
            .build()
            .getTestApi()

    // TODO test with mocked retry factory for wrapped/unwrapped

    @Test
    fun successfulCall() {
        val server = MockWebServer()
        server.enqueue(MockResponse().setBody(readResource("slack-response-ok.json")))

        server.runServer {
            val testApi = getApi(server)
            val response = testApi.getSlackResponse()

            assertThat(response).containsExactly("hello", "world")
        }
    }

}