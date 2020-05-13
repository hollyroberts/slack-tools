package retrofit

import org.apache.logging.log4j.kotlin.Logging
import retrofit.SlackTier.TIER_4
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
    fun getPaginatedStringList() = SlackApi.retrievePaginatedList(
            "strings",
            pageRetrievalFun = {
                getPaginatedPage(it)
            },
            postRetrievalFun = {
                logger.info { "Hello world! Size: ${it.size}" }
            }
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