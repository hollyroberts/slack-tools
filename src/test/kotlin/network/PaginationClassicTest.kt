package network

import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import utils.Log
import utils.TestLogHelper
import utils.TestUtils

class PaginationClassicTest : TestUtils {
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

    @Test
    fun testLogging() {
        val logHelper = TestLogHelper.forClass(Pagination::class.java)
        val logHelperTest = TestLogHelper.forClass(RetrofitTestApi::class.java)
        logHelper.setLevel(Log.LOW)
        logHelperTest.setLevel(Log.LOW)

        val server = MockWebServer()
        server.enqueue(MockResponse().setBody(readResource("string-list1.json")))
        server.enqueue(MockResponse().setBody(readResource("string-list2.json")))

        server.runServer {
            getApi(server).getPaginatedStringList()
        }

        assertThat(logHelper.events())
                .extracting<String> { it.message.formattedMessage }
                .containsExactly(
                        "Retrieved 3/5 strings (page 1/2)",
                        "Retrieved 5/5 strings (page 2/2)"
                )
        assertThat(logHelperTest.events())
                .extracting<String> { it.message.formattedMessage }
                .containsExactly("Hello world! Size: 5")

        logHelper.close()
        logHelperTest.close()
    }

}