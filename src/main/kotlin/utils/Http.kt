package utils

import slackjson.SlackResponse
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonDataException
import okhttp3.*
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.security.MessageDigest
import kotlin.system.exitProcess

class Http(authToken: String? = null) {
    companion object {
        private const val RETRY_ATTEMPTS = 3
    }

    private val client = if (authToken == null) {
        OkHttpClient()
    } else {
        OkHttpClient.Builder()
                .addNetworkInterceptor { chain ->
                    chain.proceed(chain.request()
                            .newBuilder()
                            .header("Authorization", "Bearer $authToken")
                            .build())
                }.build()
    }

    /** Enum to indicate method response */
    enum class GETStatus { SUCCESS, FAILURE, RATE_LIMITED }

    /**
     * Controls how conflicts with existing files are handled when files are downloaded
     *      IGNORE - Skips the file
     *      OVERWRITE - Overwrites the existing file
     *      HASH - Compares the hashes of the 2 files. If they differ then the downloaded file is saved as filename (2) etc.
     * */
    enum class ConflictStrategy {
        IGNORE, OVERWRITE, HASH;

        companion object {
            fun default() = OVERWRITE
        }
    }


    /**
     * Downloads a file
     * @return Whether the operation was successful or not
     */
    fun downloadFile(url: String, saveLoc: Path, size: Long? = null, strategy: ConflictStrategy = ConflictStrategy.default(),
                     authToken: String? = null): DownloadStatus {
        // Intermediary vars
        val fileExists = saveLoc.toFile().exists()
        val sizeStr = if (size != null) "(${formatSize(size)})" else "(unknown size)"

        if (!fileExists) {
            Log.medium("Downloading: '$url' as '${saveLoc.fileName}' $sizeStr")
        } else {
            when (strategy) {
                ConflictStrategy.IGNORE -> {
                    Log.low("File exists already: '${saveLoc.fileName}'")
                    return DownloadStatus.ALREADY_EXISTED
                }
                ConflictStrategy.OVERWRITE -> Log.medium("Downloading and overwriting $url as '${saveLoc.fileName}' $sizeStr")
                ConflictStrategy.HASH -> Log.medium("Downloading $url $sizeStr and then handling conflict with '${saveLoc.fileName}'")
            }
        }

        // Download file to response
        val response = try {
            val requestBuilder = Request.Builder().url(url)
            if (authToken != null) requestBuilder.addHeader("Authorization", "Bearer $authToken")
            val request = requestBuilder.build()

            Log.debugHigh("Retrieving file from URL: '$url'")
            val response = client.newCall(request).execute()
            val code = response.code()

            Log.debugLow("Response code: $code")
            if (code != 200) {
                Log.error("Code was not 200 when downloading file (given $code)")
                return DownloadStatus.FAILURE
            }

            response
        } catch (e: IOException) {
            Log.error("Error downloading file. ${e.javaClass.canonicalName}: ${e.message}")
            return DownloadStatus.FAILURE
        }

        var actualSaveLoc = saveLoc
        val downloadedBytes = response.body()!!.bytes()

        // Save to disk
        if (strategy == ConflictStrategy.HASH) {
            // Hash downloaded file
            val hashAlgorithm = MessageDigest.getInstance("SHA-256")
            val downloadedHash by lazy { hashAlgorithm.digest(downloadedBytes) }

            var extraFileCount = 0

            // Ensure that we either have the same hash as an existing file, or a unique filename
            do {
                extraFileCount++
                if (!actualSaveLoc.toFile().exists()) {
                    Log.debugLow("'$actualSaveLoc' doesn't exist")
                    break
                }

                Log.debugLow("Downloaded hash: " + bytesToHex(downloadedHash))
                val fileHash = hashAlgorithm.digest(Files.readAllBytes(actualSaveLoc))
                Log.debugLow("File hash of '$actualSaveLoc': " + bytesToHex(fileHash))

                if (fileHash contentEquals downloadedHash) {
                    Log.debugLow("Hashes are the same, ignoring")
                    return DownloadStatus.ALREADY_EXISTED
                }

                val newFileName = renameFilename(saveLoc.fileName.toString(), " ($extraFileCount)")
                actualSaveLoc = saveLoc.parent.resolve(newFileName)
            } while(true)

            if (saveLoc != actualSaveLoc) {
                Log.medium("New save location: '$actualSaveLoc'")
            }
        }

        // Just save
        Log.debugLow("Writing to $actualSaveLoc")
        actualSaveLoc.parent?.toFile()?.mkdirs()
        Files.write(actualSaveLoc, downloadedBytes)

        return when (strategy) {
            ConflictStrategy.IGNORE -> DownloadStatus.SUCCESS
            ConflictStrategy.OVERWRITE -> DownloadStatus.SUCCESS_OVERWRITE
            ConflictStrategy.HASH -> DownloadStatus.SUCCESS // TODO change
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
                GETStatus.SUCCESS -> return Result.Success(json!!)
                GETStatus.FAILURE -> {
                    Log.error("Exiting due to response failure")
                    exitProcess(-1)
                }
                GETStatus.RATE_LIMITED -> Log.warn("Rate limited, waiting " + "%,d".format(waitTime) + "s")
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
    private fun <T : SlackResponse> getInternal(url: String, adapter: JsonAdapter<T>, params: Map<String, String>): Pair<GETStatus, T?> {
        // Add params to url (including token)
        val httpUrl = HttpUrl.parse(url)!!.newBuilder()
        params.forEach { (key, value) ->
            httpUrl.addQueryParameter(key, value)
        }

        // Build request
        val request = Request.Builder()
                .url(httpUrl.build())
                .build()

        // Send request
        try {
            Log.debugHigh("GET '$httpUrl'")
            client.newCall(request).execute().use {
                return processResponse(it, adapter, url)
            }
        } catch (e: IOException) {
            Log.error(e.toString())
            return Pair(GETStatus.FAILURE, null)
        }
    }

    /**
     * Handles the checking of the response
     */
    private fun <T : SlackResponse> processResponse(response: Response, adapter: JsonAdapter<T>, url: String): Pair<GETStatus, T?> {
        val errBaseMsg = "Request for '$url' failed."

        // HTTP status codes
        if (response.code() == 429) {
            Log.warn("$errBaseMsg Rate limited.")
            return Pair(GETStatus.RATE_LIMITED, null)
        }
        if (response.code() != 200) {
            Log.error("Request for '$url' failed. Status code: " + response.code().toString() + " (" + response.message() + ")")
            return Pair(GETStatus.FAILURE, null)
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
            return Pair(GETStatus.FAILURE, null)
        }

        // Check for warnings, but do not fail on them
        if (parsedJson.warning != null) {
            Log.warn("Slack response contained warning for request '$url'. Message: " + parsedJson.warning)
        }

        return Pair(GETStatus.SUCCESS, parsedJson)
    }
}