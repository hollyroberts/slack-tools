package retrofit

enum class SlackRetry(val waitTimeMillis: Long) {
    TIER_1(60_000),
    TIER_2(3_000),
    TIER_3(1_200),
    TIER_4(600)
}