package network

import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Named

class TokenInterceptor @Inject constructor(
    @Named("SlackToken")
    private val token: String
) : Interceptor {
  override fun intercept(chain: Interceptor.Chain): Response {
    return chain.proceed(chain.request()
        .newBuilder()
        .header("Authorization", "Bearer $token")
        .build()
    )
  }
}