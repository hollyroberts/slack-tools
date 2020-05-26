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

    @JvmDefault
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


    @GET("string.cursor")
    @Slack(TIER_4)
    fun getCursorPage(@Query("cursor") cursor: String?): BasicCursorResponse

    @JvmDefault
    fun getCursorStringMap() = Pagination.retrieveCursorResponseAsMap(
            "cursorStrings",
            pageRetrievalFun = { getCursorPage(it) },
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