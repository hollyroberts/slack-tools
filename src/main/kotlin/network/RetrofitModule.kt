package network

import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import network.RetrofitModule.Base
import network.RetrofitModule.Defaults
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
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
                @Named("SlackUrl") baseUrl: HttpUrl,
                okHttpClient: OkHttpClient,
                moshi: Moshi,
                slackFactory: SlackAdapter.Factory,
                retryFactory: RetryAdapter.Factory
        ): Retrofit {
            return Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .addConverterFactory(MoshiConverterFactory.create(moshi))
                    .addCallAdapterFactory(slackFactory)
                    .addCallAdapterFactory(retryFactory)
                    .client(okHttpClient)
                    .build()
        }

        @Provides
        @Singleton
        fun provideClient(tokenInterceptor: TokenInterceptor): OkHttpClient {
            return OkHttpClient.Builder()
                    .addInterceptor(tokenInterceptor)
                    .build()
        }
    }
}