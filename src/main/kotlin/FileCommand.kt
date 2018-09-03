import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.requireObject

class FileCommand: CliktCommand(
        name = "file",
        help = "Downloads files from slack") {

    private val config by requireObject<Map<String, String>>()

    override fun run() {
        val token = config["token"]
        println(token)
    }
}