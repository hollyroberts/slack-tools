package scripts

import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.parameters.groups.OptionGroup
import com.github.ajalt.clikt.parameters.options.Option
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.enum
import com.github.ajalt.clikt.parsers.OptionParser
import utils.Log
import utils.Log.SupportedLevel

class TopLevelOptions : OptionGroup(
    name = "Top level options",
    help = "Options that are common across all scripts") {

  private val logMode by option("--log", "-l",
      metavar = SupportedLevel.values().userOptions(),
      help = "The logging level to be used"
  ).enum<SupportedLevel>()

  override fun finalize(context: Context, invocationsByOption: Map<Option, List<OptionParser.Invocation>>) {
    super.finalize(context, invocationsByOption)
    logMode?.let { Log.setLevel(it) }
  }
}