import com.beust.klaxon.JsonObject
import com.beust.klaxon.Klaxon
import com.beust.klaxon.Parser
import com.sun.org.apache.xpath.internal.operations.Bool
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.io.StringReader

object Http {
    val client = OkHttpClient()
    var token = ""

    // URLs
    const val URL_USERS_LIST = "https://slack.com/api/users.listt"

    // Enum to pass from getAndCheck to get
    enum class Status { SUCCESS, FAILURE, RATE_LIMITED }

    /**
     * Base function for GET requests
     * Encodes params and sends, then uses processResponse to return a pair of status and string (null if not success)
     *
     * @throws IOException
     */
    fun encodeAndGet(url: String, params: Map<String, String>) : Pair<Status, JsonObject?> {
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
            client.newCall(request).execute().use {
                return processResponse(it, url)
            }
        } catch (e: IOException) {
            Log.error(e.toString())
            return Pair(Status.FAILURE, null)
        }
    }

    /**
     * Handles the checking of the response
     */
    private fun processResponse(response: Response, url: String) : Pair<Status, JsonObject?> {
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

        // Parse JSON to klaxon representation
        // Body is guaranteed to be non-null if called from execute()
        val body = response.body()!!.string()
        val json = Klaxon().parseJsonObject(StringReader(body))

        if (!(json.getOrDefault("ok", false) as Boolean)) {
            var msg = "$errBaseMsg Did not receive true OK"
            if (json.containsKey("error")) {
                msg += "Error message given: " + json["error"]
            }

            Log.error(msg)
            return Pair(Status.FAILURE, null)
        }
        
        return Pair(Status.SUCCESS, json)
        /*
        val a = System.currentTimeMillis()
        println(json.boolean("ok"))
        val t = Klaxon()
                .converter(ProfileConverter)
                .parseFromJsonObject<User>(json.array<JsonObject>("members")!![0])
        println(t)
        println((System.currentTimeMillis() - a).toString())
        */
    }
}