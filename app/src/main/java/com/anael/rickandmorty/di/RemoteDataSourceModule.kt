package com.anael.rickandmorty.di

import com.anael.rickandmorty.data.remote.RnMApiRemoteDataSource
import com.anael.rickandmorty.data.remote.RetrofitRnMApiRemoteDataSource
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * DI - Hilt needs this so whenever something needs a RnMApiRemoteDataSource,
 * it provide an instance of RetrofitRnMApiRemoteDataSource instead
 * Binding for our interface to impl
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RemoteDataSourceModule {
    @Binds
    @Singleton
    abstract fun bindEpisodesRemoteDataSource(
        impl: RetrofitRnMApiRemoteDataSource
    ): RnMApiRemoteDataSource
}
