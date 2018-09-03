import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.requireObject
import utils.Api

class FileCommand: CliktCommand(
        name = "file",
        help = "Downloads files from slack") {

    override fun run() {
        val filesRaw = Api.getFiles()
    }
}