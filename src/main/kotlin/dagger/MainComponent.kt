package dagger

import retrofit.RetrofitModule
import slack.Settings
import slack.SlackData
import slack.SlackWebApi
import slackjson.MoshiModule
import utils.WebApi
import javax.inject.Named
import javax.inject.Singleton

@Singleton
@Component(
        modules = [
            MainModule::class,
            MoshiModule::class,
            RetrofitModule::class,
            RetrofitModule.Defaults::class
        ]
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

    fun getSlackWebApi(): SlackWebApi
    fun getWebApi(): WebApi
}

@Module
object MainModule {
    @Singleton
    @Provides
    fun provideSlackData(slackWebApi: SlackWebApi): SlackData {
        return slackWebApi
    }
}