package slackjson

import com.squareup.moshi.Moshi
import dagger.BindsInstance
import dagger.Component
import slack.Settings
import slack.SlackData
import javax.inject.Singleton

@Singleton
@Component(modules = [MoshiModule::class])
interface TestComponent {
    @Component.Builder
    interface Builder {
        @BindsInstance
        fun slackData(slackData: SlackData): Builder

        @BindsInstance
        fun settings(settings: Settings): Builder

        fun build(): TestComponent
    }

    fun getMoshi(): Moshi
}