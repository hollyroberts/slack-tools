package scripts

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import slack.*
import com.github.ajalt.clikt.parameters.groups.provideDelegate

fun main(args: Array<String>) = ScriptDownloadFiles().main(args)

class ScriptDownloadFiles : CliktCommand(
        name="download-files"
) {
    // Top level options
    private val topLevelArgs by TopLevelOptions()
    private val timeArgs by TimeOptions()

    //
    private val token by argument(
            help="Authorisation token for slacks web api"
    )

    override fun run() {
        topLevelArgs.run()
        timeArgs.options()

        val settings = Settings()
        val slack = SlackWebApi(token, settings)

        // TODO
//        Log.high(Instant.now().minusSeconds(86_400).epochSecond.toString())
//
//        val parsedFiles = slack.api.getFiles(startTime = Instant.now().minusSeconds(86_400).epochSecond)
//        val completeFiles = parsedFiles.toCompleteFiles(slack).filesByConvo()
//        completeFiles.downloadFiles(slack, Paths.get("files"), slack.api)
    }

}