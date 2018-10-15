package utils

import slackjson.SlackResponse
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonDataException
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import slackjson.DownloadStatus
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import kotlin.system.exitProcess

class Http(val token: String) {
    companion object {
        private const val RETRY_ATTEMPTS = 3
    }

    private val client = OkHttpClient()

    // Enums to indicate method response
    enum class GetStatus { SUCCESS, FAILURE, RATE_LIMITED }

    init {
        // Register token with Log
        Log.addToken(token)
    }

    /**
     * Downloads a file
     * @return Whether the operation was successful or not
     */
    fun downloadFile(url: String, saveLoc: Path, size: Long? = null, ignoreIfExists: Boolean = true) : DownloadStatus {
        // Don't overwrite files
        val fileExists = saveLoc.toFile().exists()
        if (fileExists && ignoreIfExists) {
            Log.low("File exists already: '${saveLoc.fileName}'")
            return DownloadStatus.ALREADY_EXISTED
        }

        Log.medium("Downloading: '$url' as '${saveLoc.fileName}'"
            + if (size != null) " (${formatSize(size)})" else " (unknown size)")

        val request = Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer $token")
                .build()

        try {
            // Download file using GET request
            Log.debugHigh("Retrieving file from URL: '$url'")
            val response = client.newCall(request).execute()
            val code = response.code()

            Log.debugLow("Response code: $code")
            if (code != 200) {
                Log.error("Code was not 200 when downloading file (given $code")
                return DownloadStatus.FAILURE
            }

            // Save to disk
            Log.debugLow("Writing to $saveLoc")
            saveLoc.parent?.toFile()?.mkdirs()
            response.body()!!.byteStream().use {
                Files.copy(it, saveLoc, StandardCopyOption.REPLACE_EXISTING)
            }

            return if (fileExists) {
                DownloadStatus.SUCCESS_OVERWRITE
            } else {
                DownloadStatus.SUCCESS
            }
        } catch (e: IOException) {
            Log.error("Error downloading file. ${e.javaClass.canonicalName}: ${e.message}")
            return DownloadStatus.FAILURE
        }
    }

    /**
     * Sends GET request to (slack) url and verifies basic json
     * @return A JsonObject result. Will always be success as right now any failure in getting data will cause the program to exit
     */
    fun <T : SlackResponse> get(url: String, adapter: JsonAdapter<T>, params: Map<String, String> = mapOf(), waitTime: Int = 1): Result<T?> {
        for (i in 1..RETRY_ATTEMPTS) {
            val (status, json) = getInternal(url, adapter, params)

            // Handle status codes
            when (status) {
                GetStatus.SUCCESS -> return Result.Success(json!!)
                GetStatus.FAILURE -> {
                    Log.error("Exiting due to response failure")
                    exitProcess(-1)
                }
                GetStatus.RATE_LIMITED -> Log.warn("Rate limited, waiting " + "%,d".format(waitTime) + "s")
            }

            if (i < RETRY_ATTEMPTS) {
                Thread.sleep(waitTime.toLong() * 1000)
                Log.medium("Retrying (" + ordinal(i + 1) + " attempt)")
            }
        }

        Log.error("Number of retry attempts exceeded")
        exitProcess(-1)
        // return utils.Result.Failure
    }

    /**
     * Internal base function for GET requests
     * Encodes params and sends, then uses processResponse to return a pair of status and string (null if not success)
     *
     * @throws IOException
     */
    private fun <T : SlackResponse> getInternal(url: String, adapter: JsonAdapter<T>, params: Map<String, String>): Pair<GetStatus, T?> {
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
            Log.debugHigh("GET '" + httpUrl.toString() + "'")
            client.newCall(request).execute().use {
                return processResponse(it, adapter, url)
            }
        } catch (e: IOException) {
            Log.error(e.toString())
            return Pair(GetStatus.FAILURE, null)
        }
    }

    /**
     * Handles the checking of the response
     */
    private fun <T : SlackResponse> processResponse(response: Response, adapter: JsonAdapter<T>, url: String): Pair<GetStatus, T?> {
        val errBaseMsg = "Request for '$url' failed."

        // HTTP status codes
        if (response.code() == 429) {
            Log.warn("$errBaseMsg Rate limited.")
            return Pair(GetStatus.RATE_LIMITED, null)
        }
        if (response.code() != 200) {
            Log.error("Request for '$url' failed. Status code: " + response.code().toString() + " (" + response.message() + ")")
            return Pair(GetStatus.FAILURE, null)
        }

        // Parse JSON to moshi representation
        // Body is guaranteed to be non-null if called from execute()
        val json = response.body()!!.string()
        Log.debugLow("Parsing JSON")
        val parsedJson = try {
            adapter.fromJson(json)!!
        } catch (e: JsonDataException) {
            Log.error(e.localizedMessage)
            Log.high("Json received: \n" + prettyFormat(json))
            throw e
        }
        Log.debugLow("JSON parsed")

        if (!parsedJson.ok) {
            var msg = "$errBaseMsg OK field was false or missing"
            if (parsedJson.error != null) {
                msg += ". Failure message given: " + parsedJson.error
            }

            Log.error(msg)
            return Pair(GetStatus.FAILURE, null)
        }

        // Check for warnings, but do not fail on them
        if (parsedJson.warning != null) {
            Log.warn("Slack response contained warning for request '$url'. Message: " + parsedJson.warning)
        }

        return Pair(GetStatus.SUCCESS, parsedJson)
    }
}