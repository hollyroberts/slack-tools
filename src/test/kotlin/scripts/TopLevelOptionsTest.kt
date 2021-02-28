package scripts

import com.github.ajalt.clikt.core.BadParameterValue
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import org.apache.logging.log4j.core.config.Configurator
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import utils.Log

internal class TopLevelOptionsTest {
  @Test
  fun loggingSetsLevel() {
    mockkStatic(Configurator::class)

    val wrapper = Wrapper()
    wrapper.parse(listOf("--log", "lOw"))
    verify { Configurator.setRootLevel(Log.LOW) }

    unmockkStatic(Configurator::class)
    Configurator.reconfigure()
  }

  @Test
  fun loggingBadArgs() {
    val wrapper = Wrapper()
    assertThatThrownBy { wrapper.parse(listOf("--log", "something")) }
        .isInstanceOf(BadParameterValue::class.java)
        .hasMessage("Invalid value for \"--log\": invalid choice: something. (choose from TRACE, DEBUG, LOW, MEDIUM, HIGH, WARN, ERROR)")
  }

  private class Wrapper : CliktCommand() {
    val topLevelOptions by TopLevelOptions()

    override fun run() {
    }
  }

}