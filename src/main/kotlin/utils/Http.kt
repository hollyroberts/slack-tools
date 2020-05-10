package utils

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonDataException
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.apache.logging.log4j.kotlin.Logging
import slackjson.SlackResponse
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.security.MessageDigest
import kotlin.system.exitProcess

class Http(authToken: String? = null) {
    companion object : Logging {
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
            logger.info { "Downloading: '$url' as '${saveLoc.fileName}' $sizeStr" }
        } else {
            when (strategy) {
                ConflictStrategy.IGNORE -> {
                    logger.log(Log.LOW) { "File exists already: '${saveLoc.fileName}'" }
                    return DownloadStatus.ALREADY_EXISTED
                }
                ConflictStrategy.OVERWRITE -> logger.info { "Downloading and overwriting $url as '${saveLoc.fileName}' $sizeStr" }
                ConflictStrategy.HASH -> logger.info { "Downloading: '$url' $sizeStr and then handling conflict with '${saveLoc.fileName}'" }
            }
        }

        // Download file to response
        val response = try {
            val requestBuilder = Request.Builder().url(url)
            if (authToken != null) requestBuilder.addHeader("Authorization", "Bearer $authToken")
            val request = requestBuilder.build()

            logger.debug { "Retrieving file from URL: '$url'" }
            val response = client.newCall(request).execute()
            val code = response.code

            logger.trace { "Response code: $code" }
            if (code != 200) {
                logger.error { "Code was not 200 when downloading file (given $code)" }
                return DownloadStatus.FAILURE
            }

            response
        } catch (e: IOException) {
            logger.error { "Error downloading file. ${e.javaClass.canonicalName}: ${e.message}" }
            return DownloadStatus.FAILURE
        }

        var actualSaveLoc = saveLoc
        val downloadedBytes = response.body!!.bytes()

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
                    logger.trace { "'$actualSaveLoc' doesn't exist" }
                    break
                }

                logger.trace { "Downloaded hash: " + bytesToHex(downloadedHash) }
                val fileHash = hashAlgorithm.digest(Files.readAllBytes(actualSaveLoc))
                logger.trace { "File hash of '$actualSaveLoc': " + bytesToHex(fileHash) }

                if (fileHash contentEquals downloadedHash) {
                    logger.trace { "Hashes are the same, ignoring" }
                    return DownloadStatus.ALREADY_EXISTED
                }

                val newFileName = renameFilename(saveLoc.fileName.toString(), " ($extraFileCount)")
                actualSaveLoc = saveLoc.parent.resolve(newFileName)
            } while(true)

            if (saveLoc != actualSaveLoc) {
                logger.info { "New save location: '$actualSaveLoc'" }
            }
        }

        // Just save
        logger.trace { "Writing to $actualSaveLoc" }
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
                    logger.error { "Exiting due to response failure" }
                    exitProcess(-1)
                }
                GETStatus.RATE_LIMITED -> logger.warn { "Rate limited, waiting " + "%,d".format(waitTime) + "s" }
            }

            if (i < RETRY_ATTEMPTS) {
                Thread.sleep(waitTime.toLong() * 1000)
                logger.info { "Retrying (" + ordinal(i + 1) + " attempt)" }
            }
        }

        logger.error { "Number of retry attempts exceeded" }
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
        val httpUrl = url.toHttpUrlOrNull()!!.newBuilder()
        params.forEach { (key, value) ->
            httpUrl.addQueryParameter(key, value)
        }

        // Build request
        val request = Request.Builder()
                .url(httpUrl.build())
                .build()

        // Send request
        try {
            logger.debug { "GET '$httpUrl'" }
            client.newCall(request).execute().use {
                return processResponse(it, adapter, url)
            }
        } catch (e: IOException) {
            logger.error { e.toString() }
            return Pair(GETStatus.FAILURE, null)
        }
    }

    /**
     * Handles the checking of the response
     */
    private fun <T : SlackResponse> processResponse(response: Response, adapter: JsonAdapter<T>, url: String): Pair<GETStatus, T?> {
        val errBaseMsg = "Request for '$url' failed."

        // HTTP status codes
        if (response.code == 429) {
            logger.warn { "$errBaseMsg Rate limited." }
            return Pair(GETStatus.RATE_LIMITED, null)
        }
        if (response.code != 200) {
            logger.error { "Request for '$url' failed. Status code: " + response.code.toString() + " (" + response.message + ")" }
            return Pair(GETStatus.FAILURE, null)
        }

        // Parse JSON to moshi representation
        // Body is guaranteed to be non-null if called from execute()
        // TODO this could be configured somehow
        val json = response.body!!.string()
        logger.trace { "Parsing JSON" }
        val parsedJson = try {
            adapter.fromJson(json)!!
        } catch (e: JsonDataException) {
            logger.error { e.localizedMessage }
            logger.log(Log.HIGH) { "Json received: \n" + prettyFormat(json) }
            throw e
        }
        logger.trace { "JSON parsed" }

        if (!parsedJson.ok) {
            var msg = "$errBaseMsg OK field was false or missing"
            if (parsedJson.error != null) {
                msg += ". Failure message given: " + parsedJson.error
            }

            logger.error { msg }
            return Pair(GETStatus.FAILURE, null)
        }

        // Check for warnings, but do not fail on them
        if (parsedJson.warning != null) {
            logger.warn { "Slack response contained warning for request '$url'. Message: " + parsedJson.warning }
        }

        return Pair(GETStatus.SUCCESS, parsedJson)
    }
}