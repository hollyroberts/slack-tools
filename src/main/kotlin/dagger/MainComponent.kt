package dagger

import network.RetrofitModule
import network.SlackApi
import network.http.HttpUtilsModule
import slack.Settings
import slack.SlackData
import slack.UserAndConvoMap
import slackjson.MoshiModule
import javax.inject.Named
import javax.inject.Singleton

@Singleton
@Component(
        modules = [
            MainModule::class,
            MoshiModule::class,
            RetrofitModule::class,
            RetrofitModule.Defaults::class,
            HttpUtilsModule.Token::class
        ]
)
interface MainComponent {
    @Component.Builder
    interface Builder {
        @BindsInstance
        fun settings(settings: Settings): Builder

        @BindsInstance
        fun token(@Named("SlackToken") token: String): Builder

        fun build(): MainComponent
    }

    fun getSlackApi(): SlackApi

    @Deprecated(message = "To be replaced with something better I hope")
    fun getUserAndConvoMap(): UserAndConvoMap
}

@Module
object MainModule {
    @Singleton
    @Provides
    fun provideSlackData(userAndConvoMap: UserAndConvoMap): SlackData {
        return userAndConvoMap
    }
}