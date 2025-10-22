package com.anael.rickandmorty.di

import com.anael.rickandmorty.data.model.EpisodeDto
import com.anael.rickandmorty.data.model.PageInfoDto
import com.anael.rickandmorty.data.model.PagedEpisodesDto
import com.anael.rickandmorty.data.remote.RnMApiRemoteDataSource
import com.anael.rickandmorty.fakes.FakeEpisodesRemoteDataSource
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Singleton

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [RemoteDataSourceModule::class] // replaces your @Binds prod module
)
object TestRemoteDataSourceModule {

    @Provides @Singleton
    fun provideFakeRemote(): RnMApiRemoteDataSource {
        // Build some fake pages
        val page1 = PagedEpisodesDto(
            info = PageInfoDto(next = "page=2", prev = null, count = 2, pages = 2),
            results = listOf(
                EpisodeDto(1, "Pilot", "2017-12-02", "S01E01", emptyList(), "", ""),
                EpisodeDto(2, "Lawnmower Dog", "2017-12-09", "S01E02", emptyList(), "", "")
            )
        )
        val page2 = PagedEpisodesDto(
            info = PageInfoDto(next = null, prev = "page=1", count = 2, pages = 2),
            results = listOf(
                EpisodeDto(3, "Anatomy Park", "2017-12-16", "S01E03", emptyList(), "", "")
            )
        )
        return FakeEpisodesRemoteDataSource(
            pages = mapOf(1 to page1, 2 to page2)
        )
    }
}
