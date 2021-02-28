package utils

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ThreadSleeper @Inject constructor() {
  fun sleep(millis: Long) {
    Thread.sleep(millis)
  }
}