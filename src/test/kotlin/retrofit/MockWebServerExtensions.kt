package retrofit

import okhttp3.mockwebserver.MockWebServer

inline fun MockWebServer.runServer(function: () -> Unit) {
    this.start()
    try {
        function.invoke()
    } finally {
        this.shutdown()
    }
}