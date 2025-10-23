package com.anael.rickandmorty.di

import com.anael.rickandmorty.domain.model.CharacterRnM
import com.anael.rickandmorty.domain.repository.CharacterRepository
import com.anael.rickandmorty.domain.repository.EpisodesRepository
import com.anael.rickandmorty.fakes.FakeEpisodesRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Singleton

@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [RepositoryModule::class]
)
@Module
object TestRepositoryModule {

    @Provides @Singleton
    fun provideEpisodesRepository(fake: FakeEpisodesRepository): EpisodesRepository = fake

    @Provides @Singleton
    fun provideCharacterRepository(): CharacterRepository = object : CharacterRepository {
        override suspend fun getCharacter(id: String) =
            Result.failure<CharacterRnM>(Throwable("not used in EpisodesScreen tests"))

        override suspend fun getCharactersByIds(ids: List<String>) =
            Result.success<List<CharacterRnM>>(emptyList())
    }
}
