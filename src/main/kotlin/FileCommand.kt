import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.requireObject
import slackjson.CompleteFile
import utils.Api

class FileCommand: CliktCommand(
        name = "file",
        help = "Downloads files from slack") {

    override fun run() {
        val filesRaw = Api.getFiles().toMutableList()

        filesRaw.replaceAll { CompleteFile.create(it) }
    }
}