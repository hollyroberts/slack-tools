package network

import dagger.BindsInstance
import dagger.Component
import okhttp3.HttpUrl
import javax.inject.Named
import javax.inject.Singleton

@Singleton
@Component(modules = [RetrofitModule.Base::class, RetrofitTestModule::class])
interface RetrofitTestComponent {
    @Component.Builder
    interface Builder {
        @BindsInstance
        fun url(@Named("SlackUrl") url: HttpUrl): Builder

        fun build(): RetrofitTestComponent
    }

    fun getTestApi(): RetrofitTestApi
}