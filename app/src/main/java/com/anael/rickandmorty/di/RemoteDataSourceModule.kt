package com.anael.rickandmorty.di

import com.anael.rickandmorty.data.remote.EpisodesRemoteDataSource
import com.anael.rickandmorty.data.remote.RetrofitEpisodesRemoteDataSource
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RemoteDataSourceModule {
    @Binds
    @Singleton
    abstract fun bindEpisodesRemoteDataSource(
        impl: RetrofitEpisodesRemoteDataSource
    ): EpisodesRemoteDataSource
}
