import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import slackjson.CompleteFile
import slackjson.SlackFile
import utils.Api
import utils.Log
import java.nio.file.Path
import java.nio.file.Paths

object FileCommand: CliktCommand(
        name = "file",
        help = "Downloads files from slack") {

    // Args/Opts
    private val noInfer by option("-ni", "--no-infer",
            help = "Disable channel inference using list API data, as edge cases can exist. " +
                    "Instead performs an API call per file (slow)").flag()
    private val output by option("-o", "--output",
            help = "Output directory for files. Extends the base directory set").default("files")

    // Constants
    private const val LOCATION_INTERVAL = 3000

    /**
     * Downloads all files from slack
     */
    override fun run() {
        val filesRaw: MutableList<SlackFile> = Api.getFiles().toMutableList()

        // Start timer to output process every X seconds
        Log.min("Locating upload location of files (this may take a while, especially if inference is disabled)")
        val startTime = System.currentTimeMillis()
        var nextOutputTime = startTime + LOCATION_INTERVAL

        // Iterate over objects, and replace with complete version
        for ((index, obj) in filesRaw.withIndex()) {
            filesRaw[index] = CompleteFile(obj, !noInfer)

            if (System.currentTimeMillis() > nextOutputTime) {
                Log.info("Processed ${index + 1}/${filesRaw.size} files")
                nextOutputTime = System.currentTimeMillis() + LOCATION_INTERVAL
            }
        }

        // Output timed messages if took more than LOCATION_INTERVAL
        val timeTaken = System.currentTimeMillis() - startTime
        if (timeTaken > LOCATION_INTERVAL) {
            Log.min(String.format("Located the upload location of all files in %,.1f seconds", timeTaken.toFloat() / 1000))
        } else {
            Log.info("Files located")
        }

        // Download files
        val outDir = SlackTools.folderPath(output)
    }
}