package retrofit

import retrofit.SlackTier.TIER_3
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query
import slackjson.FileListResponse

interface SlackApi {
    @GET("files.list?count=100")
    @Tier(TIER_3)
    fun listFiles(
            @Query("ts_from") startTime: Long? = null,
            @Query("ts_to") endTime: Long? = null,
            @Query("channel") channel: String? = null,
            @Query("user") user: String? = null
    ): Call<FileListResponse>
}