import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UserListResponse(
        val members: List<User>
) : CursorResponse()

@JsonClass(generateAdapter = true)
data class FileListResponse(
        val files: List<File>
) : PaginatedResponse()

@JsonClass(generateAdapter = true)
data class FileReponse (
        val file: File
) : SlackResponse()