package com.anael.rickandmorty.data.paging

import com.anael.rickandmorty.data.local.AppDatabase
import com.anael.rickandmorty.data.remote.RnMApiService
import javax.inject.Inject

class EpisodesRemoteMediatorFactory @Inject constructor(
    private val service: RnMApiService,
    private val db: AppDatabase
) {
    fun create(): EpisodesRemoteMediator = EpisodesRemoteMediator(service, db)
}
