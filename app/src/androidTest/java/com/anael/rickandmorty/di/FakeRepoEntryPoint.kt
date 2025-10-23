package com.anael.rickandmorty.di

import com.anael.rickandmorty.fakes.FakeEpisodesRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import android.content.Context

@EntryPoint
@InstallIn(SingletonComponent::class)
interface FakeRepoEntryPoint {
    fun repo(): FakeEpisodesRepository
}

// helper for tests
fun Context.fakeEpisodesRepo(): FakeEpisodesRepository =
    EntryPointAccessors.fromApplication(this, FakeRepoEntryPoint::class.java).repo()
