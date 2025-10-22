package com.anael.rickandmorty.data.paging

import com.anael.rickandmorty.data.local.AppDatabase
import com.anael.rickandmorty.data.remote.EpisodesRemoteDataSource
import javax.inject.Inject

class EpisodesRemoteMediatorFactory @Inject constructor(
    private val remote: EpisodesRemoteDataSource,
    private val db: AppDatabase
) {
    fun create() = EpisodesRemoteMediator(remote, db)
}