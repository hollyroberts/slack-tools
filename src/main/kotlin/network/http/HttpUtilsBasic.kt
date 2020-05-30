package network.http

import okhttp3.OkHttpClient
import javax.inject.Inject

class HttpUtilsBasic @Inject constructor(): HttpUtils(OkHttpClient())