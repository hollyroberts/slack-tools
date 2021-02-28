package dagger

import json.MoshiModule
import network.RetrofitModule
import network.SlackApi
import network.http.HttpUtilsModule
import slack.Settings
import slack.SlackData
import slack.UserAndConvoMap
import javax.inject.Named
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
      WebMainComponent.Declarations::class,
      MoshiModule::class,
      RetrofitModule::class,
      RetrofitModule.Defaults::class,
      HttpUtilsModule.Token::class
    ]
)
interface WebMainComponent {
  @Component.Builder
  interface Builder {
    @BindsInstance
    fun settings(settings: Settings): Builder

    @BindsInstance
    fun token(@Named("SlackToken") token: String): Builder

    fun build(): WebMainComponent
  }

  fun getSlackApi(): SlackApi

  @Deprecated(message = "To be replaced with something better I hope")
  fun getUserAndConvoMap(): UserAndConvoMap

  @Module
  interface Declarations {
    @Binds
    fun provideSlackData(userAndConvoMap: UserAndConvoMap): SlackData
  }
}