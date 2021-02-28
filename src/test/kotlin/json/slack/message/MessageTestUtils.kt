package json.slack.message

import io.mockk.mockk
import json.DaggerTestComponent
import slack.Settings

internal object MessageTestUtils {
  val moshi = DaggerTestComponent.builder()
      .settings(Settings())
      .slackData(mockk())
      .api(mockk())
      .build()
      .getMoshi()
}