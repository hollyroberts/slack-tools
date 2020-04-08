package slackjson

import dagger.MainComponent
import dagger.Module
import dagger.Provides
import javax.inject.Named

@Module
object TestModule {
    @Provides
    fun getComponent(testComponent: TestComponent): MainComponent = testComponent

    @Provides
    @Named("token")
    fun getToken() = "placeholder-token"
}