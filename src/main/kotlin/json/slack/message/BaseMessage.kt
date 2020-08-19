package json.slack.message

import java.math.BigDecimal

abstract class BaseMessage {
    lateinit var ts: BigDecimal

    @Transient
    lateinit var subtype: MessageType
}

abstract class BaseUserMessage : BaseMessage() {
    lateinit var user: String
}

