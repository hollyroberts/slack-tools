package dagger

import slack.Settings
import slack.SlackWebApi
import utils.WebApi
import javax.inject.Named
import javax.inject.Singleton

@Singleton
@Component
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