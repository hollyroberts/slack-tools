import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.findObject
import com.github.ajalt.clikt.core.requireObject
import com.github.ajalt.clikt.parameters.arguments.argument

/**
 * Top level command for download commands, so each sub command has access to the token
 */
class DownloadCommand : CliktCommand(
        name = "download",
        help = "Download files, avatars, and private/dm message history") {

    private val token by argument()
    private val config by findObject { mutableMapOf<String, String>() }

    override fun run() {
        config["token"] = token
    }
}

class FileCommand: CliktCommand(
        name = "file",
        help = "Downloads files from slack") {

    private val config by requireObject<Map<String, String>>()

    override fun run() {
        val token = config["token"]
        println(token)
    }
}