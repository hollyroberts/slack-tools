package slackjson

import com.squareup.moshi.Moshi
import dagger.MembersInjector
import dagger.Module
import dagger.Provides
import dagger.multibindings.ClassKey
import dagger.multibindings.IntoMap
import slackjson.message.BaseMessageCustomAdapter
import javax.inject.Singleton
import kotlin.reflect.KClass

typealias InjectionMap = Map<KClass<out Any>, MembersInjector<out Any>>

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
                    .add(ConversationContextfulAdapter)
                    .build()

    @Provides
    @IntoMap
    @ClassKey(Conversation::class)
    fun mapConversation(dmInjector: MembersInjector<ConversationDm>): InjectionMap {
        return mapOf(ConversationDm::class to dmInjector)
    }

    @Provides
    @IntoMap
    @ClassKey(ParsedFile::class)
    fun mapParsedFile(injector: MembersInjector<ParsedFile>): InjectionMap {
        return basicInjection(injector)
    }

    private inline fun <reified T : Any> basicInjection(injector: MembersInjector<T>): InjectionMap {
        return mapOf(T::class to injector)
    }

}