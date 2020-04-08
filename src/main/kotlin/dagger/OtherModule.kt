package dagger

import slack.SlackData
import slack.SlackWebApi
import javax.inject.Singleton

@Module
class OtherModule {
    @Singleton
    @Provides
    fun provideSlackData(slackWebApi: SlackWebApi): SlackData {
        return slackWebApi;
    }
}