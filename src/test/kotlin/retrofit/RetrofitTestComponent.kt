package retrofit

import dagger.BindsInstance
import dagger.Component
import okhttp3.HttpUrl
import slack.Settings
import slack.SlackData
import slackjson.MoshiModule
import javax.inject.Named
import javax.inject.Singleton

@Singleton
@Component(modules = [MoshiModule::class, RetrofitModule::class])
interface RetrofitTestComponent {
    @Component.Builder
    interface Builder {
        @BindsInstance
        fun url(@Named("SlackUrl") url: HttpUrl): Builder

        @BindsInstance
        fun slackData(slackData: SlackData): Builder

        @BindsInstance
        fun settings(settings: Settings): Builder

        fun build(): RetrofitTestComponent
    }

    fun getRetrofit(): SlackApi
}