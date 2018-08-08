import utils.Api
import utils.Http

fun main(args: Array<String>) {
    val token = args[0]
    Http.token = token
    println("Token: $token")

    // utils.Api.getUsers()
    val files = Api.getFiles()
    for (file in files) {
        file.retrieveIncompleteData()
        println(file.uploadLocation)
    }
}