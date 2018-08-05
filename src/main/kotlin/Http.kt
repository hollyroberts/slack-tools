import com.squareup.moshi.JsonAdapter
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import kotlin.system.exitProcess

object Http {
    private val client = OkHttpClient()
    var token = ""

    // Internal Enum to pass between methods
    enum class Status { SUCCESS, FAILURE, RATE_LIMITED }

    /**
     * Sends GET request to (slack) url and verifies basic json
     * @return A JsonObject result. Will always be success as right now any failure in getting data will cause the program to exit
     */
    fun <T : SlackResponse> get(url: String, adapter: JsonAdapter<T>, params: Map<String, String> = mapOf(), rateLimitAttempts: Int = 2): Result.Success<T?> {
        require(rateLimitAttempts >= 1)

        for (i in 1..rateLimitAttempts) {
            val (status, json) = getInternal(url, adapter, params)

            // Handle status codes
            when (status) {
                Status.SUCCESS -> return Result.Success(json!!)
                Status.FAILURE -> {
                    Log.error("Exiting due to response failure")
                    exitProcess(-1)
                }
                Status.RATE_LIMITED -> {
                    if (i < rateLimitAttempts) {
                        println("Retrying (" + ordinal(i + 1) + " attempt)")
                    } else {
                        println("Number of rate limited retry attempts exceeded")
                    }
                }
            }
        }

        exitProcess(-1)
        // return Result.Failure
    }

    /**
     * Internal base function for GET requests
     * Encodes params and sends, then uses processResponse to return a pair of status and string (null if not success)
     *
     * @throws IOException
     */
    private fun <T : SlackResponse> getInternal(url: String, adapter: JsonAdapter<T>, params: Map<String, String>): Pair<Status, T?> {
        // Add params to url (including token)
        val httpUrl = HttpUrl.parse(url)!!.newBuilder()
        httpUrl.addQueryParameter("token", token)
        params.forEach { key, value ->
            httpUrl.addQueryParameter(key, value)
        }

        // Build request
        val request = Request.Builder()
                .url(httpUrl.build())
                .build()

        // Send request
        try {
            Log.debug("GET '" + httpUrl.toString() + "'")
            client.newCall(request).execute().use {
                return processResponse(it, adapter, url)
            }
        } catch (e: IOException) {
            Log.error(e.toString())
            return Pair(Status.FAILURE, null)
        }
    }

    /**
     * Handles the checking of the response
     */
    private fun <T : SlackResponse> processResponse(response: Response, adapter: JsonAdapter<T>, url: String): Pair<Status, T?> {
        val errBaseMsg = "Request for '$url' failed."

        // HTTP status codes
        if (response.code() == 429) {
            Log.warn("$errBaseMsg Rate limited.")
            return Pair(Status.RATE_LIMITED, null)
        }
        if (response.code() != 200) {
            Log.error("Request for '$url' failed. Status code: " + response.code().toString() + " (" + response.message() + ")")
            return Pair(Status.FAILURE, null)
        }

        // Parse JSON to moshi representation
        // Body is guaranteed to be non-null if called from execute()
        val json = response.body()!!.string()
        val parsedJson = adapter.fromJson(json)!!

        if (!parsedJson.ok) {
            var msg = "$errBaseMsg OK field was false or missing"
            if (parsedJson.error != null) {
                msg += ". Failure message given: " + parsedJson.error
            }

            Log.error(msg)
            return Pair(Status.FAILURE, null)
        }

        // Check for warnings, but do not fail on them
        if (parsedJson.warning != null) {
            Log.warn("Slack response contained warning for request '$url'. Message: " + parsedJson.warning)
        }

        return Pair(Status.SUCCESS, parsedJson)
    }
}