package network.http

import okhttp3.OkHttpClient
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HttpUtilsToken @Inject constructor(
        tokenClient: OkHttpClient
): HttpUtils(tokenClient)