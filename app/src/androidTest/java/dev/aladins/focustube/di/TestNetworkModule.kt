package dev.aladins.focustube.di

import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import dev.aladins.focustube.data.remote.quota.QuotaTracker
import dev.aladins.focustube.data.remote.youtube.YouTubeApi
import dev.aladins.focustube.testing.FakeYouTubeApi
import javax.inject.Singleton
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient

/**
 * UI tests never call YouTube: the API is [FakeYouTubeApi] (tests inject it to inspect calls),
 * behind the same daily-limit rule as the real network stack.
 */
@Module
@TestInstallIn(components = [SingletonComponent::class], replaces = [NetworkModule::class])
object TestNetworkModule {
    @Provides
    @Singleton
    fun provideJson(): Json = Json { ignoreUnknownKeys = true }

    @Provides
    @Singleton
    @PlainHttpClient
    fun providePlainClient(): OkHttpClient = OkHttpClient()

    @Provides
    @Singleton
    fun provideFakeApi(): FakeYouTubeApi = FakeYouTubeApi()

    @Provides
    fun provideApi(fake: FakeYouTubeApi, tracker: QuotaTracker): YouTubeApi = QuotaCheckingYouTubeApi(fake, tracker)
}
