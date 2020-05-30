package network.http

import okhttp3.OkHttpClient
import javax.inject.Inject

class HttpUtilsToken @Inject constructor(
        tokenClient: OkHttpClient
): HttpUtils(tokenClient)