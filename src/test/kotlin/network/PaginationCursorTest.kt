package network

import network.BasicCursorResponse.CursorResponseContents
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import utils.TestUtils
import java.util.concurrent.TimeUnit

class PaginationCursorTest : TestUtils {
    private fun getApi(server: MockWebServer) = DaggerRetrofitTestComponent.builder()
            .url(server.url(""))
            .build()
            .getTestApi()

    @Test
    fun successfulMultiCall() {
        val server = MockWebServer()
        server.enqueue(MockResponse().setBody(readResource("cursor-response1.json")))
        server.enqueue(MockResponse().setBody(readResource("cursor-response2.json")))

        server.runServer {
            val testApi = getApi(server)
            val response = testApi.getCursorStringMap()

            assertThat(response).containsExactlyInAnyOrderEntriesOf(mapOf(
                    "one" to CursorResponseContents("one", "two"),
                    "three" to CursorResponseContents("three", "four"),
                    "5" to CursorResponseContents("5", "6")
            ))
        }

        server.takeRequest(0L, TimeUnit.MILLISECONDS)
        val lastRequest = server.takeRequest(0L, TimeUnit.MILLISECONDS)!!
        val url = lastRequest.requestUrl!!
        assertThat(url.queryParameter("cursor"))
                .isNotNull()
                .isEqualTo("dXNlcjpVMEc5V0ZYTlo=")
        assertThat(server.requestCount).isEqualTo(2)
    }

    @Test
    fun singleCallNoCursorInMetadata() {
        val server = MockWebServer()
        server.enqueue(MockResponse().setBody(readResource("cursor-response-single.json")))

        server.runServer {
            val testApi = getApi(server)
            val response = testApi.getCursorStringMap()

            assertThat(response).containsExactlyInAnyOrderEntriesOf(mapOf(
                    "hello" to CursorResponseContents("hello", "world"),
                    "alpha" to CursorResponseContents("alpha", "bravo")
            ))
        }

        assertThat(server.requestCount).isEqualTo(1)
    }

}