import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UserList(
        val members: List<User>
) : CursorResponse()

@JsonClass(generateAdapter = true)
data class FileList(
        val files: List<File>
) : PaginatedResponse()