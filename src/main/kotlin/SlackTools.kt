import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import slack.SlackExport
import utils.Http
import java.nio.file.Path
import java.nio.file.Paths

fun main(arguments: Array<String>) = SlackTools
        .subcommands(
                ExportProcessor,
                DownloadCommand.subcommands(FileCommand)
        ).main(arguments)

// Wrapper for subcommands (because clikt wants us to do it this way)
object SlackTools : CliktCommand() {
    private val baseDir by option("-bo", "--base-output",
            help = "Base output directory for files. Use this to avoid typing the same path multiple times").default("")

    override fun run() = Unit

    fun folderPath(subfolder: String) : Path {
        return Paths.get(baseDir).resolve(subfolder)
    }
}

/**
 * Top level command for download commands, so each sub command has access to the token
 */
object DownloadCommand : CliktCommand(
        name = "download",
        help = "Download files, avatars, and private/dm message history") {

    private val token by argument()

    override fun run() {
        Http.token = token
    }
}

object ExportProcessor: CliktCommand(
        name = "process-export",
        help = "Processes a folder of JSON files from a slack export. Options available include converting to text files, generating statistics, etc.") {

    val exportJson by option("-ej", "--export-json",
            help = "Convert the JSON files per channel into a single file")

    override fun run() {
        SlackExport.loadFromFolder(Paths.get("clan"))
    }
}