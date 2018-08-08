import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.arguments.argument
import utils.Http

fun main(arguments: Array<String>) = ExportProcessor()
        .subcommands(ExportProcessor())
        .main(arguments)

class SlackTools : CliktCommand() {
    override fun run() = Unit
}

class ExportProcessor: CliktCommand(
        name = "process-export",
        help = "Takes a folder of JSON files from a slack export. Options available include converting to text files, generating statistics, etc.") {

    override fun run() {
    }
}