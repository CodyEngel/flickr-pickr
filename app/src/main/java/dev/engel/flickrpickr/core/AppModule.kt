package dev.engel.flickrpickr.core

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.engel.flickrpickr.BuildConfig
import javax.inject.Qualifier
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    @FlickrApiKey
    fun provideFlickrApiKey(): String = BuildConfig.FLICKR_API_KEY

    @Provides
    @Singleton
    @IsDebug
    fun provideIsDebug(): Boolean = BuildConfig.DEBUG
}

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class FlickrApiKey

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class IsDebug