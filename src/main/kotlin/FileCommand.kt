import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.requireObject
import slackjson.CompleteFile
import slackjson.SlackFile
import utils.Api

class FileCommand: CliktCommand(
        name = "file",
        help = "Downloads files from slack") {

    override fun run() {
        val filesRaw: MutableList<SlackFile> = Api.getFiles().toMutableList()

        filesRaw.replaceAll { CompleteFile(it) }
    }
}