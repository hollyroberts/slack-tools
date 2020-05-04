package retrofit

import retrofit2.Call
import retrofit2.CallAdapter
import retrofit2.Retrofit
import slackjson.SlackResponse
import slackjson.SlackSimpleResponse
import java.lang.reflect.Type
import javax.inject.Inject

class SlackAdapter {
    class Factory @Inject constructor(
            private val retryFactory: RetryAdapter.Factory
    ) : CallAdapter.Factory() {
        override fun get(returnType: Type, annotations: Array<Annotation>, retrofit: Retrofit): CallAdapter<*, *>? {
            val wrapper = annotations.find { it is UseWrapper } as UseWrapper?
            val type: Type = wrapper?.clazz?.java ?: returnType

            if (!SlackResponse::class.java.isAssignableFrom(getRawType(type))) {
                return null
            }

            val retryAdapter = retryFactory.get(type, annotations, retrofit) ?: return null
            return if (wrapper != null) {
                @Suppress("UNCHECKED_CAST")
                return UnwrapAdapter(type, retryAdapter as RetryAdapter<SlackSimpleResponse<Any>>)
            } else {
                @Suppress("UNCHECKED_CAST")
                PlainAdapter(type, retryAdapter as RetryAdapter<SlackResponse>)
            }
        }
    }

    class PlainAdapter<T: SlackResponse>(
            private val responseType: Type,
            private val retryAdapter: RetryAdapter<T>
    ) : CallAdapter<T, Any> {
        override fun responseType(): Type = responseType

        override fun adapt(call: Call<T>): T {
            val response = retryAdapter.adapt(call)
            response.verify()

            return response
        }
    }

    class UnwrapAdapter<R: SlackSimpleResponse<T>, T>(
            private val responseType: Type,
            private val retryAdapter: RetryAdapter<R>
    ) : CallAdapter<R, Any> {
        override fun responseType(): Type = responseType

        override fun adapt(call: Call<R>): T {
            val response = retryAdapter.adapt(call)
            response.verify()

            return response.getContents()
        }
    }
}