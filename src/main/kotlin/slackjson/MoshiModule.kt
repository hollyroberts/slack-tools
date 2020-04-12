package slackjson

import com.squareup.moshi.Moshi
import dagger.MembersInjector
import dagger.Module
import dagger.Provides
import dagger.multibindings.ClassKey
import dagger.multibindings.IntoMap
import slackjson.message.BaseMessageCustomAdapter
import javax.inject.Singleton

@Module
object MoshiModule {
    @Provides
    @Singleton
    fun provideMoshi(injectorFactory: InjectorAdapter.JsonFactory): Moshi =
            Moshi.Builder()
                    .add(injectorFactory)
                    .add(BaseMessageCustomAdapter)
                    .add(ProfileJsonAdapter)
                    .add(ShareJsonAdapter)
                    .build()

    @Provides
    @IntoMap
    @ClassKey(Conversation::class)
    fun mapConversation(injector: MembersInjector<Conversation>): MembersInjector<out Any> {
        return injector
    }

    @Provides
    @IntoMap
    @ClassKey(ParsedFile::class)
    fun mapParsedFile(injector: MembersInjector<ParsedFile>): MembersInjector<out Any> {
        return injector
    }

}