import com.github.ajalt.clikt.core.CliktCommand
import slackjson.CompleteFile
import slackjson.SlackFile
import utils.Api
import utils.Log

object FileCommand: CliktCommand(
        name = "file",
        help = "Downloads files from slack") {

    override fun run() {
        val filesRaw: MutableList<SlackFile> = Api.getFiles().toMutableList()

        Log.info("Locating upload location of files (this may take a while, especially if inference is disabled)")
        // Start timer to output process every X seconds
        filesRaw.replaceAll { CompleteFile(it) }
    }
}