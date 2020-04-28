package retrofit

import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import utils.TestUtils

class PaginationTest : TestUtils {
    private fun getApi(server: MockWebServer) = DaggerRetrofitTestComponent.builder()
            .url(server.url(""))
            .build()
            .getTestApi()

    @Test
    fun successfulCall() {
        val server = MockWebServer()
        server.enqueue(MockResponse().setBody(readResource("string-list1.json")))
        server.enqueue(MockResponse().setBody(readResource("string-list2.json")))

        server.runServer {
            val testApi = getApi(server)
            val response = testApi.getPaginatedStringList()

            assertThat(response).containsExactly("one", "three", "two", "four", "five")
        }
    }

}