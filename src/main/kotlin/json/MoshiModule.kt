package json

import com.squareup.moshi.Moshi
import dagger.BindsOptionalOf
import dagger.MembersInjector
import dagger.Module
import dagger.Provides
import dagger.multibindings.ClassKey
import dagger.multibindings.IntoMap
import json.slack.file.ParsedFile
import json.slack.file.ShareJsonAdapter
import json.slack.message.SlackBotMessageAdapter
import json.slack.message.SlackMessageAdapter
import json.slack.metadata.Conversation
import json.slack.metadata.ConversationContextfulAdapter
import json.slack.metadata.ConversationDm
import json.slack.metadata.ProfileJsonAdapter
import network.SlackApi
import org.apache.logging.log4j.kotlin.Logging
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
  fun provideMoshi(
      injectorFactory: InjectorAdapter.JsonFactory,
      slackMessageAdapter: SlackMessageAdapter
  ): Moshi {
    logger.debug { "Initialising moshi" }
    val moshi = Moshi.Builder()
        // Factories
        .add(injectorFactory)
        .add(NullDroppingList.Factory)
        .add(slackMessageAdapter)

        // Slack json adapters
        .add(ProfileJsonAdapter)
        .add(ShareJsonAdapter)
        .add(ConversationContextfulAdapter)
        .add(SlackBotMessageAdapter)

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