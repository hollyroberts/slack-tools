package network

import network.RetryAdapter.CallResult.FailureMode.RATE_LIMITED
import network.RetryAdapter.CallResult.FailureMode.UNKNOWN
import org.apache.logging.log4j.kotlin.Logging
import retrofit2.Call
import retrofit2.CallAdapter
import retrofit2.Retrofit
import utils.ThreadSleeper
import java.lang.reflect.Type
import javax.inject.Inject

class RetryAdapter<T>(
    private val responseType: Type,
    private val retryTier: SlackTier,
    private val threadSleeper: ThreadSleeper
) : CallAdapter<T, Any> {
  class Factory @Inject constructor(
      private val threadSleeper: ThreadSleeper
  ) : CallAdapter.Factory() {
    override fun get(returnType: Type, annotations: Array<Annotation>, retrofit: Retrofit): CallAdapter<*, *>? {
      val annotationTier = (annotations.find { it is Slack } ?: return null) as Slack

      return RetryAdapter<Any>(returnType, annotationTier.tier, threadSleeper)
    }
  }

  companion object : Logging {
    const val MAX_ATTEMPTS = 3
  }

  private sealed class CallResult<out R> {
    data class Success<out T>(val body: T) : CallResult<T>()
    data class Failure(val reason: FailureMode) : CallResult<Nothing>()

    enum class FailureMode { UNKNOWN, RATE_LIMITED }
  }

  override fun responseType(): Type = responseType

  override fun adapt(call: Call<T>): T {
    for (attempt in 1..MAX_ATTEMPTS) {
      val newCall = if (attempt == 1) call else call.clone()
      val response = executeCall(newCall) { logger.warn { it } }

      if (response is CallResult.Success) {
        // TODO check this first
        return response.body
      }

      when ((response as CallResult.Failure).reason) {
        RATE_LIMITED -> {
          logger.warn { "Rate limited, waiting " + "%,d".format(retryTier.waitTimeMillis) + "ms" }
          if (attempt < MAX_ATTEMPTS) threadSleeper.sleep(retryTier.waitTimeMillis)
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

      // TODO handle this better
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
