package retrofit

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
    fun getPaginatedStringList() = SlackApi.retrievePaginatedList("strings") {
        getPaginatedPage(it)
    }

    @GET("slack.response")
    @Slack(TIER_4)
    @UseWrapper(StringListResponse::class)
    fun getSlackResponse(): List<String>
}