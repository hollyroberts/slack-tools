package retrofit

import retrofit.SlackAdapter.CallResult.FailureMode.RATE_LIMITED
import retrofit.SlackAdapter.CallResult.FailureMode.UNKNOWN
import retrofit2.Call
import retrofit2.CallAdapter
import retrofit2.Retrofit
import utils.Log
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

internal class SlackAdapter<T>(
        private val responseType: Type,
        private val retryTier: SlackTier
) : CallAdapter<T, Any> {
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
            if (type.rawType != SlackResult::class.java) {
                return null
            }

            val annotationTier = (annotations.find { it is Tier }
                    ?: throw IllegalStateException("Retrofit interfaces returning ${SlackResult::class.simpleName} must specify a method tier")
            ) as Tier

            // TODO process to ensure slack response contains ok
            return SlackAdapter<Any>(type.actualTypeArguments[0], annotationTier.tier)
        }
    }

    companion object {
        const val MAX_ATTEMPTS = 3
    }

    private sealed class CallResult<out R> {
        data class Success<out T>(val body: T) : CallResult<T>()
        data class Failure(val reason: FailureMode) : CallResult<Nothing>()

        enum class FailureMode { UNKNOWN, RATE_LIMITED }
    }

    override fun responseType(): Type = responseType

    override fun adapt(call: Call<T>): SlackResult<T> {
        for (attempt in 1..MAX_ATTEMPTS) {
            val newCall = if (attempt == 1) call else call.clone()
            val response = executeCall(newCall) { Log.warn(it) }

            if (response is CallResult.Success) {
                return SlackResult(response.body)
            }

            when ((response as CallResult.Failure).reason) {
                RATE_LIMITED -> {
                    if (attempt < MAX_ATTEMPTS) Thread.sleep(retryTier.waitTimeMillis)
                }
                UNKNOWN -> throw RuntimeException("Unknown error occurred calling ${call.request().url.encodedPath}")
            }
        }

        throw RuntimeException("Call to ${call.request().url.encodedPath} failed after $MAX_ATTEMPTS attempts")
    }

    private fun executeCall(call: Call<T>, logFun: (String) -> Unit): CallResult<T> {
        val response = call.execute()
        // TODO handle IO Exception?

        // Handle status codes
        if (!response.isSuccessful) {
            val url = call.request().url.toString()
            val statusCode = response.code()

            logFun.invoke("Request to '$url' was unsuccessful")
            logFun.invoke("Status code: $statusCode (${response.message()})")

            val errBody = response.errorBody()?.string()
            if (!errBody.isNullOrEmpty()) {
                logFun.invoke("Error response body:\n${errBody}")
            }

            return when (statusCode) {
                429 -> CallResult.Failure(RATE_LIMITED)
                else -> CallResult.Failure(UNKNOWN)
            }
        }

        val body = response.body() ?: return CallResult.Failure(UNKNOWN)
        return CallResult.Success(body)
    }
}
