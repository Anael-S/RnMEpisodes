package com.anael.rickandmorty.di

import com.anael.rickandmorty.data.repository.CharacterRepositoryImpl
import com.anael.rickandmorty.data.repository.EpisodesRepositoryImpl
import com.anael.rickandmorty.domain.repository.CharacterRepository
import com.anael.rickandmorty.domain.repository.EpisodesRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindCharacterRepository(
        impl: CharacterRepositoryImpl
    ): CharacterRepository

    @Binds
    @Singleton
    abstract fun bindEpisodesRepository(
        impl: EpisodesRepositoryImpl
    ): EpisodesRepository
}
