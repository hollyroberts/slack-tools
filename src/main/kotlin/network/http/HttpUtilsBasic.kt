package network.http

import okhttp3.OkHttpClient
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HttpUtilsBasic @Inject constructor() : HttpUtils(OkHttpClient())