package network

import com.squareup.moshi.JsonDataException
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import io.github.classgraph.ClassGraph
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import json.SlackSimpleResponse
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
      .threadSleeper(mockk())
      .build()
      .getTestApi()

  private fun getApi(server: MockWebServer) = getApi(server.url(""))

  @Test
  fun returnsNoAdapter() {
    val retryFactory: RetryAdapter.Factory = mockk()
    val testApi = getTestApiWithMockedFactory(retryFactory)

    every { retryFactory.get(any(), any(), any()) } returns null

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

  @Test
  fun okFalseWithWarningAndErrors() {
    val server = MockWebServer()
    server.enqueue(MockResponse().setBody(readResource("slack-response-bad-1.json")))

    server.runServer {
      val testApi = getApi(server)
      assertThatThrownBy { testApi.getSlackResponse() }
          .isInstanceOf(JsonDataException::class.java)
          .hasMessage("Response from slack did not indicate success" +
              "\n\t\tWarning message: This is a warning" +
              "\n\t\tError message: This is an error")
    }
  }

  @Test
  fun okFalseNoData() {
    val server = MockWebServer()
    server.enqueue(MockResponse().setBody(readResource("slack-response-bad-2.json")))

    server.runServer {
      val testApi = getApi(server)
      assertThatThrownBy { testApi.getSlackResponse() }
          .isInstanceOf(JsonDataException::class.java)
          .hasMessage("Response from slack did not indicate success. No information about the failure was provided.")
    }
  }

  @Test
  fun okButNoContents() {
    val server = MockWebServer()
    server.enqueue(MockResponse().setBody(readResource("slack-response-bad-3.json")))

    server.runServer {
      val testApi = getApi(server)
      assertThatThrownBy { testApi.getSlackResponse() }
          .isInstanceOf(JsonDataException::class.java)
          .hasMessage("Response from call to '/slack.response' did not contain field 'list'")
    }
  }

  @Test
  fun goodDataNoOk() {
    val server = MockWebServer()
    server.enqueue(MockResponse().setBody(readResource("slack-response-bad-4.json")))

    server.runServer {
      val testApi = getApi(server)
      assertThatThrownBy { testApi.getSlackResponse() }
          .isInstanceOf(JsonDataException::class.java)
          .hasMessage("Response from slack did not indicate success. No information about the failure was provided.")
    }
  }

  /**
   * Kotlin needs a field specifier for constructor parameters for us to see the annotation (as it doesn't know if it's a field/getter/setter)
   * So this test ensures that we add the annotation on all instances of SlackSimpleResponse
   */
  @Test
  fun slackSimpleResponseAnnotations() {
    val scanResult = ClassGraph().enableAllInfo().scan()
    val subclasses = scanResult.getSubclasses(SlackSimpleResponse::class.java.canonicalName)

    subclasses.forEach { subclass ->
      val contentsAnnotation = subclass.getFieldInfo("contents")!!.annotationInfo

      assertThat(contentsAnnotation).anySatisfy { annotation ->
        assertThat(annotation.name).isEqualTo("com.squareup.moshi.Json")
        assertThat(annotation.parameterValues).anySatisfy {
          assertThat(it.name).isEqualTo("name")
        }
      }
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


