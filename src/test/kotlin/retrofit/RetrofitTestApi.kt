package retrofit

import retrofit.SlackTier.TIER_4
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface RetrofitTestApi {
    @GET("path.test?count=100")
    fun pathTest(@Query("fake_user") user: String? = null): Call<List<String>>

    @GET("retry.test")
    @Tier(TIER_4)
    fun retryTest(): SlackResult<List<String>>

    @GET("annotation.test")
    fun annotationTest(): SlackResult<List<String>>

    @GET("string.list.page")
    @Tier(TIER_4)
    fun getPaginatedPage(@Query("page") page: Int): SlackResult<BasicPaginatedResponse>

    @JvmDefault
    fun getPaginatedStringList() = SlackApi.retrievePaginatedList("strings") {
        getPaginatedPage(it).result
    }
}