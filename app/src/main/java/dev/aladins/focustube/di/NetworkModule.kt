package dev.aladins.focustube.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.Multibinds
import dev.aladins.focustube.BuildConfig
import dev.aladins.focustube.data.remote.quota.QuotaInterceptor
import dev.aladins.focustube.data.remote.quota.QuotaTracker
import dev.aladins.focustube.data.remote.youtube.YouTubeApi
import dev.aladins.focustube.data.remote.youtube.YouTubeKeyInterceptor
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier
import javax.inject.Singleton
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

/** Extra interceptors added last (debug builds add a key-redacting HTTP logger). */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class DebugInterceptors

/** OkHttp client for images and other non-YouTube-API requests: never carries the key. */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class PlainHttpClient

/** Declares the (possibly empty) set of debug interceptors. */
@Module
@InstallIn(SingletonComponent::class)
interface DebugInterceptorsDeclarations {
    @Multibinds
    @DebugInterceptors
    fun debugInterceptors(): Set<Interceptor>
}

/** Network stack: JSON, OkHttp (key + quota interceptors) and the YouTube Retrofit API. */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    private const val TIMEOUT_SECONDS = 20L

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
    }

    @Provides
    @Singleton
    @PlainHttpClient
    fun providePlainClient(): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .build()

    @Provides
    @Singleton
    fun provideYouTubeClient(
        @PlainHttpClient plain: OkHttpClient,
        tracker: QuotaTracker,
        @DebugInterceptors debugInterceptors: Set<@JvmSuppressWildcards Interceptor>
    ): OkHttpClient = plain.newBuilder()
        .addInterceptor(YouTubeKeyInterceptor(BuildConfig.YOUTUBE_API_KEY))
        .addInterceptor(QuotaInterceptor(tracker))
        .apply { debugInterceptors.forEach { addInterceptor(it) } }
        .build()

    @Provides
    @Singleton
    fun provideYouTubeApi(client: OkHttpClient, json: Json): YouTubeApi = Retrofit.Builder()
        .baseUrl(YouTubeApi.BASE_URL)
        .client(client)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()
        .create(YouTubeApi::class.java)
}
