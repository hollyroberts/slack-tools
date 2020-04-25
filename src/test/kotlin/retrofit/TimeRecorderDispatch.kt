package retrofit

import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.RecordedRequest
import java.time.Instant
import java.util.*
import kotlin.collections.ArrayList

class TimeRecorderDispatcher : Dispatcher() {
    private val queue: Queue<MockResponse> = ArrayDeque()
    val dispatchTimes: MutableList<Instant> = ArrayList()

    fun addResponse(mockResponse: MockResponse) {
        queue.add(mockResponse)
    }

    override fun dispatch(request: RecordedRequest): MockResponse {
        dispatchTimes.add(Instant.now())
        return queue.remove()
    }
}