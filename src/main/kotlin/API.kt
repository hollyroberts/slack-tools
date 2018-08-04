import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request

object API {
    val client = OkHttpClient()
    var token = ""

    // URLs
    const val URL_USERS_LIST = "https://slack.com/api/users.list"

    fun get(url: String, params: Map<String, String> = mapOf()) {
        // Add params to url
        val httpUrl = HttpUrl.parse(url)!!.newBuilder()
        httpUrl.addQueryParameter("token", token)
        params.forEach { key, value ->
            httpUrl.addQueryParameter(key, value)
        }

        val request = Request.Builder()
                .url(httpUrl.build())
                .build()

        val response = client.newCall(request).execute()
        println(response)
        println(response.body()!!.string())
    }
}