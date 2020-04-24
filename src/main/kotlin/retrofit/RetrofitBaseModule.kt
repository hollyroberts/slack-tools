package retrofit

import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.create
import javax.inject.Named
import javax.inject.Singleton

@Module
object RetrofitModule {
    @Module
    object Constants {
        @Provides
        @Named("SlackUrl")
        @Singleton
        fun provideBaseUrl() = "https://slack.com/api/".toHttpUrl()
    }


    @Provides
    @Singleton
    fun provideRetrofitBuilder(
            moshi: Moshi,
            @Named("SlackUrl") baseUrl: HttpUrl
    ): Retrofit {
        return Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()
        // TODO add some interceptors
    }

    @Provides
    @Singleton
    fun provideRetrofitService(retrofit: Retrofit): SlackApi = retrofit.create()
}