package retrofit

import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import retrofit2.Retrofit
import retrofit2.create
import javax.inject.Singleton

@Module
object RetrofitTestModule {
    @Provides
    @Singleton
    fun provideTestService(retrofit: Retrofit): RetrofitTestApi = retrofit.create()

    @Provides
    @Singleton
    fun provideMoshi() = Moshi.Builder().build()
}