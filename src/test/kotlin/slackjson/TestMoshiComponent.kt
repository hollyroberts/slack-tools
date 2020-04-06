package slackjson

import com.squareup.moshi.Moshi
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [MoshiModule::class])
interface TestMoshiComponent {
    fun getMoshi(): Moshi
}