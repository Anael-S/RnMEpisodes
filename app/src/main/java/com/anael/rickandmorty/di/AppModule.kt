package com.anael.rickandmorty.di

import android.content.Context
import androidx.room.Room
import com.anael.rickandmorty.data.remote.RnMApiService
import com.anael.rickandmorty.data.repository.EpisodesRepositoryImpl
import com.anael.rickandmorty.data.local.AppDatabase
import com.anael.rickandmorty.data.local.EpisodeDao
import com.anael.rickandmorty.data.local.EpisodeRemoteKeyDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "episodes.db")
            .fallbackToDestructiveMigration() // Wipe everything if it changes, easier here
            .build()

    @Provides fun provideEpisodeDao(db: AppDatabase): EpisodeDao = db.episodeDao()
    @Provides fun provideEpisodeRemoteKeyDao(db: AppDatabase): EpisodeRemoteKeyDao = db.episodeRemoteKeyDao()

    @Provides @Singleton
    fun provideEpisodesService(): RnMApiService = RnMApiService.create()

    @Provides @Singleton
    fun provideRepository(
        service: RnMApiService,
        db: AppDatabase
    ): EpisodesRepositoryImpl = EpisodesRepositoryImpl(service, db)


}
