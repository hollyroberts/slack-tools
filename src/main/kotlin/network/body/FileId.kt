package network.body

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class FileId(val file: String)