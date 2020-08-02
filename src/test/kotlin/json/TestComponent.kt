package json

import com.squareup.moshi.Moshi
import dagger.BindsInstance
import dagger.Component
import network.SlackApi
import network.http.HttpUtilsModule
import slack.Settings
import slack.SlackData
import javax.inject.Singleton

@Singleton
@Component(modules = [MoshiModule::class, HttpUtilsModule.Basic::class])
interface TestComponent {
    @Component.Builder
    interface Builder {
        @BindsInstance
        fun slackData(slackData: SlackData): Builder

        @BindsInstance
        fun settings(settings: Settings): Builder

        @BindsInstance
        fun api(slackApi: SlackApi): Builder

        fun build(): TestComponent
    }

    fun getMoshi(): Moshi
}