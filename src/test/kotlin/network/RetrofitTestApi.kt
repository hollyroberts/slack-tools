package network

import network.SlackTier.TIER_4
import org.apache.logging.log4j.kotlin.Logging
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface RetrofitTestApi {
  @GET("path.test?count=100")
  fun pathTest(@Query("fake_user") user: String? = null): Call<List<String>>

  @GET("retry.test")
  @Slack(TIER_4)
  fun retryTest(): List<String>

  @GET("annotation.test")
  fun annotationTest(): List<String>

  @GET("string.list.page")
  @Slack(TIER_4)
  fun getPaginatedPage(@Query("page") page: Int): BasicPaginatedResponse

  fun getPaginatedStringList(): List<String> {
    val results = Pagination.retrievePaginatedList(
        "strings",
        pageRetrievalFun = {
          getPaginatedPage(it)
        }
    )
    logger.info { "Hello world! Size: ${results.size}" }
    return results
  }

  @GET("cursor.list")
  @Slack(TIER_4)
  fun getCursorListPage(@Query("cursor") cursor: String?): TestCursorListResponse

  fun getCursorList() = Pagination.retrieveCursorResponseAsList(
      name = "cursorStringsList",
      pageRetrievalFun = { getCursorListPage(it) },
      appenderFun = { it.asSequence().filterNotNull() }
  )

  @GET("cursor.map")
  @Slack(TIER_4)
  fun getCursorMapPage(@Query("cursor") cursor: String?): TestCursorMapResponse

  fun getCursorMap() = Pagination.retrieveCursorResponseAsMap(
      "cursorStrings",
      pageRetrievalFun = { getCursorMapPage(it) },
      mappingFun = { it.a }
  )

  @GET("slack.response")
  @Slack(TIER_4)
  @UseWrapper(StringListResponse::class)
  fun getSlackResponse(): List<String>

  @GET("no.adapter")
  @Slack(TIER_4)
  fun noAdapters(): List<String>

  companion object : Logging
}