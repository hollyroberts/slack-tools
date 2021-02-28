package utils

interface TestUtils {
  fun readResource(resource: String): String {
    val inputStream = this::class.java.getResourceAsStream(resource)!!
    return inputStream.reader().readText()
  }
}