package com.anael.rickandmorty.di

import com.anael.rickandmorty.domain.repository.EpisodesRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface EpisodesRepoProbeEntryPoint {
    fun episodesRepo(): EpisodesRepository
}
