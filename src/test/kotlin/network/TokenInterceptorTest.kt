package network

import com.squareup.moshi.Moshi
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import okhttp3.HttpUrl
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import retrofit2.Retrofit
import retrofit2.create
import utils.TestUtils
import javax.inject.Named
import javax.inject.Singleton

internal class TokenInterceptorTest : TestUtils {
    private fun getApi(server: MockWebServer) = DaggerTokenInterceptorTest_TestComponent.builder()
            .url(server.url(""))
            .token("test_token")
            .build()
            .getTestApi()

    @Test
    fun addsHeaders() {
        val server = MockWebServer()
        server.enqueue(MockResponse().setBody(readResource("slack-response-ok.json")))

        server.runServer {
            val testApi = getApi(server)
            testApi.getSlackResponse()
        }

        assertThat(server.takeRequest().headers)
                .contains(
                        "Content-Type" to "application/json",
                        "Authorization" to "Bearer test_token"
                )
    }

    @Singleton
    @Component(modules = [RetrofitModule.Base::class, TestModule::class])
    interface TestComponent {
        @Component.Builder
        interface Builder {
            @BindsInstance
            fun url(@Named("SlackUrl") httpUrl: HttpUrl): Builder

            @BindsInstance
            fun token(@Named("SlackToken") token: String): Builder

            fun build(): TestComponent
        }

        fun getTestApi(): RetrofitTestApi
    }

    @Module
    object TestModule {
        @Provides
        @Singleton
        fun provideTestService(retrofit: Retrofit): RetrofitTestApi = retrofit.create()

        @Provides
        @Singleton
        fun provideMoshi(): Moshi = Moshi.Builder().build()
    }
}