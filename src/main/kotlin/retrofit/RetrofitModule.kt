package retrofit

import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import retrofit.RetrofitModule.Base
import retrofit.RetrofitModule.Defaults
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.create
import javax.inject.Named
import javax.inject.Singleton

@Module(includes = [Base::class, Defaults::class])
object RetrofitModule {
    @Module
    object Defaults {
        @Provides
        @Named("SlackUrl")
        @Singleton
        fun provideBaseUrl() = "https://slack.com/api/".toHttpUrl()

        @Provides
        @Singleton
        fun provideRetrofitService(retrofit: Retrofit): SlackApi = retrofit.create()
    }

    @Module
    object Base {
        @Provides
        @Singleton
        fun provideRetrofitBuilder(
                moshi: Moshi,
                @Named("SlackUrl") baseUrl: HttpUrl
        ): Retrofit {
            return Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .addConverterFactory(MoshiConverterFactory.create(moshi))
                    .addCallAdapterFactory(SlackAdapter.Factory())
                    .build()
        }
    }
}