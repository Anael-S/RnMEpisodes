package com.anael.rickandmorty.data.paging

import com.anael.rickandmorty.data.local.AppDatabase
import com.anael.rickandmorty.data.remote.RnMApiRemoteDataSource
import javax.inject.Inject

/**
 * Factory for EpisodesRemoteMediator
 */
class EpisodesRemoteMediatorFactory @Inject constructor(
    private val remote: RnMApiRemoteDataSource,
    private val db: AppDatabase
) {
    fun create() = EpisodesRemoteMediator(remote, db)
}