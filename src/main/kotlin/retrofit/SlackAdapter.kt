package retrofit

import retrofit2.Call
import retrofit2.CallAdapter
import retrofit2.Response
import retrofit2.Retrofit
import utils.Log
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

internal class SlackAdapter<T>(private val responseType: Type): CallAdapter<T, Any> {
    class Factory : CallAdapter.Factory() {
        /**
         * This method is used by retrofit to handle methods
         * Importantly it has to check if the return type is SlackResult&lt;T&gt;
         * If not then this handler is not suitable
         * If so then we return the handler to be used by retrofit
         *
         * @return new SlackAdapterif the handler can process the type of the request, otherwise null
         */
        override fun get(returnType: Type, annotations: Array<Annotation>, retrofit: Retrofit): CallAdapter<*, *>? {
            val type = returnType as ParameterizedType
            return if (type.rawType != SlackResult::class.java) {
                null
            } else {
                SlackAdapter<Any>(type.actualTypeArguments[0])
            }
        }
    }

    override fun responseType(): Type = responseType

    override fun adapt(call: Call<T>): SlackResult<T> {
        // Get call url for later if there's an error, then perform request
        val url = call.request().url.toString()
        // TODO handle IOException
        val response: Response<T>? = call.execute()

        // Handle status codes
        if (!response!!.isSuccessful) {
            Log.error("Request to '$url' was unsuccessful")
            Log.error("Status code: ${response.code()} (${response.message()})")

            val errBody = response.errorBody()?.string()
            if (!errBody.isNullOrEmpty()) {
                Log.error("Error response body:\n${errBody}")
            }

            throw RuntimeException("Unsuccessful call to '$url' (code ${response.code()})")
        }

        val body = response.body() ?: throw RuntimeException("Returned body from '$url' was null")
        return SlackResult(body)
    }
}
