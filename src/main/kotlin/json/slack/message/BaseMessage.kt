package json.slack.message

import java.math.BigDecimal

interface BaseMessage {
    val ts: BigDecimal
    var subtype: MessageType
}

interface BaseUserMessage : BaseMessage {
    val user: String
}

