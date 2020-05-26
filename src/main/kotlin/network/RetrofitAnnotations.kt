package network

import slackjson.SlackSimpleResponse
import kotlin.reflect.KClass

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Slack(val tier: SlackTier)

enum class SlackTier(val waitTimeMillis: Long) {
    TIER_1(60_000),
    TIER_2(3_000),
    TIER_3(1_200),
    TIER_4(600)
}

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class UseWrapper(val clazz: KClass<out SlackSimpleResponse<*>>)