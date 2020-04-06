package slackjson

import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import slackjson.message.BaseMessageCustomAdapter


@Module
object MoshiModule {
    @Provides
    fun provideMoshi(injectorAdapter: InjectorAdapter.Factory, baseMessageCustomAdapter: BaseMessageCustomAdapter): Moshi {
        return Moshi.Builder()
            .add(injectorAdapter)
            .add(baseMessageCustomAdapter)
            .add(ProfileJsonAdapter)
            .add(ShareJsonAdapter)
            .build()
    }
}