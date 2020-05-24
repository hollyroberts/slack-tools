package utils

import java.nio.file.Path
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class WebApi @Inject constructor(
        @Named("SlackToken") token: String
) {
    private val http = Http(token)

    /**
     * Equivalent to Http.downloadFile, but manages token for us
     */
    fun downloadFile(url: String, saveLoc: Path, size: Long? = null, strategy: Http.ConflictStrategy = Http.ConflictStrategy.default()) : DownloadStatus {
        return http.downloadFile(url, saveLoc, size, strategy)
    }
}