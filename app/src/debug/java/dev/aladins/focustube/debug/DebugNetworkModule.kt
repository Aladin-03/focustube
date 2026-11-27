package dev.aladins.focustube.debug

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import dev.aladins.focustube.data.remote.youtube.YouTubeKeyInterceptor
import dev.aladins.focustube.di.DebugInterceptors
import okhttp3.Interceptor
import okhttp3.logging.HttpLoggingInterceptor

/** Debug builds only: logs YouTube request lines (BASIC) with the key replaced by ██. */
@Module
@InstallIn(SingletonComponent::class)
object DebugNetworkModule {
    @Provides
    @IntoSet
    @DebugInterceptors
    fun provideLoggingInterceptor(): Interceptor = redactedLoggingInterceptor()
}

/** HTTP logger that never prints the YouTube key (used by debug builds and the redaction test). */
fun redactedLoggingInterceptor(
    logger: HttpLoggingInterceptor.Logger = HttpLoggingInterceptor.Logger.DEFAULT
): HttpLoggingInterceptor = HttpLoggingInterceptor(logger).apply {
    level = HttpLoggingInterceptor.Level.BASIC
    redactQueryParams(YouTubeKeyInterceptor.KEY_PARAM)
}
