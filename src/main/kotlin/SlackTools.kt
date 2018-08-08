import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.options.option

fun main(arguments: Array<String>) = SlackTools()
        .subcommands(ExportProcessor())
        .main(arguments)

// Wrapper for subcommands (because clikt wants us to do it this way)
class SlackTools : CliktCommand() {
    override fun run() = Unit
}

class ExportProcessor: CliktCommand(
        name = "process-export",
        help = "Processes a folder of JSON files from a slack export. Options available include converting to text files, generating statistics, etc.") {

    val exportJson by option("-ej", "--export-json",
            help = "Convert the JSON files per channel into a single file")

    override fun run() {
    }
}