package retrofit

import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.create
import javax.inject.Singleton

@Module
object RetrofitModule {
    @Provides
    @Singleton
    fun provideRetrofitService(moshi: Moshi): SlackApi {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://slack.com/api/")
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
        return retrofit.create()
    }
}