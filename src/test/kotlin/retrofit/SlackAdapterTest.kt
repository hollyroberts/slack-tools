package retrofit

import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import utils.TestUtils
import javax.inject.Named
import javax.inject.Singleton

class SlackAdapterTest : TestUtils {
    private fun getApi(url: HttpUrl) = DaggerRetrofitTestComponent.builder()
            .url(url)
            .build()
            .getTestApi()
    private fun getApi(server: MockWebServer) = getApi(server.url(""))

    // TODO test with mocked retry factory for wrapped/unwrapped
    @Test
    fun returnsNoAdapter() {
        val retryFactory: RetryAdapter.Factory = mockk()
        val testApi = getTestApiWithMockedFactory(retryFactory)

        every { retryFactory.get(any(), any(), any() )} returns null

        assertThatThrownBy { testApi.noAdapters() }
                .isInstanceOf(IllegalArgumentException::class.java)
                .hasMessageContaining("Unable to create call adapter for java.util.List<java.lang.String>")
    }

    @Test
    fun returnsPlainAdapter() {
        val retryFactory: RetryAdapter.Factory = mockk()
        val testApi = getTestApiWithMockedFactory(retryFactory)

        assertThatThrownBy { testApi.getPaginatedPage(0) }
        verify { retryFactory.get(BasicPaginatedResponse::class.java, any(), any()) }
    }

    @Test
    fun returnsUnwrapAdapter() {
        val retryFactory: RetryAdapter.Factory = mockk()
        val testApi = getTestApiWithMockedFactory(retryFactory)

        assertThatThrownBy { testApi.getSlackResponse() }
        verify { retryFactory.get(StringListResponse::class.java, any(), any()) }
    }

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

    private fun getTestApiWithMockedFactory(retryFactory: RetryAdapter.Factory): RetrofitTestApi {
        return DaggerSlackAdapterTest_TestComponent.builder()
                .retryFactory(retryFactory)
                .build()
                .getTestApi()
    }

    @Singleton
    @Component(modules = [RetrofitModule.Base::class, RetrofitTestModule::class, TestModule::class])
    interface TestComponent {
        @Component.Builder
        interface Builder {
            @BindsInstance
            fun retryFactory(retryAdapter: RetryAdapter.Factory): Builder

            fun build(): TestComponent
        }

        fun getTestApi(): RetrofitTestApi
    }

    @Module
    object TestModule {
        @Provides
        @Named("SlackUrl")
        fun provideUrl() = "https://localhost".toHttpUrl()
    }

}


