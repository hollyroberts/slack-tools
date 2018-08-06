import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UserList(
        val members: List<User>
) : PaginatedCursor()