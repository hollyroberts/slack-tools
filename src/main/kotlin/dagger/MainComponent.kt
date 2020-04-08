package dagger

import slack.Settings
import slack.SlackWebApi
import slackjson.Conversation
import slackjson.MoshiModule
import slackjson.ParsedFile
import utils.WebApi
import javax.inject.Named
import javax.inject.Singleton

@Singleton
@Component(
        modules = [MoshiModule::class, OtherModule::class]
)
interface MainComponent {
    @Component.Builder
    interface Builder {
        @BindsInstance
        fun settings(settings: Settings): Builder

        @BindsInstance
        fun token(@Named("token") token: String): Builder

        fun build(): MainComponent
    }

    fun getThis() = this

    fun getSlackWebApi(): SlackWebApi
    fun getWebApi(): WebApi

    fun inject(conversation: Conversation)
    fun inject(parsedFile: ParsedFile)
}