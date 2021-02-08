package network

import io.mockk.mockk
import network.TestCursorMapResponse.CursorResponseContents
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import utils.TestUtils

class PaginationCursorTest : TestUtils {
    private fun getApi(server: MockWebServer) = DaggerRetrofitTestComponent.builder()
            .url(server.url(""))
            .threadSleeper(mockk())
            .build()
            .getTestApi()

    @Test
    fun cursorMapSingleCall() {
        val server = MockWebServer()
        server.enqueue(MockResponse().setBody(readResource("cursor-map-response-single.json")))

        server.runServer {
            val testApi = getApi(server)
            val response = testApi.getCursorMap()

            assertThat(response).containsExactlyInAnyOrderEntriesOf(mapOf(
                    "hello" to CursorResponseContents("hello", "world"),
                    "alpha" to CursorResponseContents("alpha", "bravo")
            ))
        }

        assertThat(server.requestCount).isEqualTo(1)
    }

    @Test
    fun cursorMapMultiCall() {
        val server = MockWebServer()
        server.enqueue(MockResponse().setBody(readResource("cursor-map-response1.json")))
        server.enqueue(MockResponse().setBody(readResource("cursor-map-response2.json")))

        server.runServer {
            val testApi = getApi(server)
            val response = testApi.getCursorMap()

            assertThat(response).containsExactlyInAnyOrderEntriesOf(mapOf(
                    "one" to CursorResponseContents("one", "two"),
                    "three" to CursorResponseContents("three", "four"),
                    "5" to CursorResponseContents("5", "6")
            ))
        }

        server.skipRequests(1)
        val lastRequest = server.takeRequestImmediately()
        val url = lastRequest.requestUrl!!
        assertThat(url.queryParameter("cursor"))
                .isNotNull()
                .isEqualTo("dXNlcjpVMEc5V0ZYTlo=")
        assertThat(server.requestCount).isEqualTo(2)
    }

    @Test
    fun cursorListSingleCall() {
        val server = MockWebServer()
        server.enqueue(MockResponse().setBody(readResource("cursor-list-response-single.json")))

        server.runServer {
            val testApi = getApi(server)
            val response = testApi.getCursorList()

            assertThat(response).containsExactly(
                    "one",
                    "three",
                    "two"
            )
        }

        assertThat(server.requestCount).isEqualTo(1)
    }

    @Test
    fun cursorListMultiCallWithFilter() {
        val server = MockWebServer()
        server.enqueue(MockResponse().setBody(readResource("cursor-list-response1.json")))
        server.enqueue(MockResponse().setBody(readResource("cursor-list-response1.json")))
        server.enqueue(MockResponse().setBody(readResource("cursor-list-response2.json")))

        server.runServer {
            val testApi = getApi(server)

            // Check that single response has nulls
            val firstPageContents = testApi.getCursorListPage(null).contents
            assertThat(firstPageContents)
                    .containsExactly("one", "three", null, "two")

            // Then assert that paginated cursor list is correct without nulls
            val response = testApi.getCursorList()
            assertThat(response).containsExactly(
                    "one", "three", "two",
                    "two", "five", "four"
            )
        }

        server.skipRequests(2)
        val lastRequest = server.takeRequestImmediately()
        val url = lastRequest.requestUrl!!
        assertThat(url.queryParameter("cursor"))
                .isNotNull()
                .isEqualTo("ASkjdhASDKLjho=")
        assertThat(server.requestCount).isEqualTo(3)
    }

}