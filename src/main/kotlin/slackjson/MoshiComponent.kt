package slackjson

import dagger.Subcomponent

@Subcomponent
interface MoshiComponent {
    @Subcomponent.Builder
    interface Builder {
        fun build(): MoshiComponent
    }

    fun inject(conversation: Conversation)
}