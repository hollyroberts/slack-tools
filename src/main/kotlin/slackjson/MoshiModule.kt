package slackjson

import com.squareup.moshi.Moshi
import dagger.BindsOptionalOf
import dagger.MembersInjector
import dagger.Module
import dagger.Provides
import dagger.multibindings.ClassKey
import dagger.multibindings.IntoMap
import network.SlackApi
import org.apache.logging.log4j.kotlin.Logging
import slackjson.message.SlackMessageAdapter
import javax.inject.Singleton
import kotlin.reflect.KClass

typealias InjectionMap = Map<KClass<out Any>, MembersInjector<out Any>>

@Module(includes = [MoshiModule.OptionalBindings::class])
object MoshiModule : Logging {
    // TODO I hope I can fix this
    @Module
    interface OptionalBindings {
        @BindsOptionalOf
        fun optionalSlackApi(): SlackApi
    }
    @Provides
    @Singleton
    fun provideMoshi(injectorFactory: InjectorAdapter.JsonFactory): Moshi {
        logger.debug { "Initialising moshi" }
        val moshi = Moshi.Builder()
                // Factories
                .add(injectorFactory)
                .add(NullDroppingList.Factory)
                .add(SlackMessageAdapter)

                // Slack json adapters
                .add(ProfileJsonAdapter)
                .add(ShareJsonAdapter)
                .add(ConversationContextfulAdapter)

                // Extra types
                .add(BigDecimalAdapter)
                .build()
        logger.debug { "Moshi initialised" }
        return moshi
    }


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