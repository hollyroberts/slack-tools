package retrofit

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface RetrofitTestApi {
    @GET("files.list?count=100")
    fun listFiles(@Query("fake_user") user: String? = null): Call<List<String>>

    @GET("retry.test")
    fun retryTest(): SlackResult<List<String>>
}