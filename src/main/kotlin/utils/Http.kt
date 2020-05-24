package utils

import okhttp3.OkHttpClient
import okhttp3.Request
import org.apache.logging.log4j.kotlin.Logging
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.security.MessageDigest

class Http(authToken: String? = null) {
    companion object : Logging

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
    fun downloadFile(
            url: String,
            saveLoc: Path,
            size: Long? = null,
            strategy: ConflictStrategy = ConflictStrategy.default(),
            authToken: String? = null
    ): DownloadStatus {
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
}