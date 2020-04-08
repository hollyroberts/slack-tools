package slackjson

import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import slackjson.message.BaseMessageCustomAdapter
import javax.inject.Singleton

@Module
object MoshiModule {
    @Provides
    @Singleton
    fun provideMoshi(injectorAdapter: InjectorAdapter.Factory): Moshi {
        return Moshi.Builder()
                .add(injectorAdapter)
                .add(BaseMessageCustomAdapter)
                .add(ProfileJsonAdapter)
                .add(ShareJsonAdapter)
                .build()
    }
}