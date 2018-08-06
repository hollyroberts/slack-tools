data class File(
        // Identication
        val id: String,
        val user: String,
        val title: String,

        // Metadata
        val filetype: String,
        val size: Long,
        val url_private_download: String,

        // Where has this file been sent
        // Won't be included if file object is directly from a channel
        val channels: List<String>?,
        val ims: List<String>
)