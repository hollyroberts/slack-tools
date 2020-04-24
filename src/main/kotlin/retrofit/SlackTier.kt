package retrofit

annotation class Tier(val tier: SlackTier)

enum class SlackTier(val waitTimeMillis: Long) {
    TIER_1(60_000),
    TIER_2(3_000),
    TIER_3(1_200),
    TIER_4(600)
}