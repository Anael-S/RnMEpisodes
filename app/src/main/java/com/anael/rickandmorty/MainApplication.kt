package com.anael.rickandmorty

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.*
import com.anael.rickandmorty.infrastructure.work.EpisodesSyncScheduler
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class MainApplication : Application(), Configuration.Provider {

  @Inject lateinit var workerFactory: HiltWorkerFactory
  @Inject lateinit var episodesSyncScheduler: EpisodesSyncScheduler

  override fun onCreate() {
    super.onCreate()

    // 1) Initialize WorkManager explicitly with Hilt’s factory
    val wmConfig = Configuration.Builder()
      .setWorkerFactory(workerFactory)
      .setMinimumLoggingLevel(
        if (BuildConfig.DEBUG) android.util.Log.DEBUG else android.util.Log.ERROR
      )
      .build()
    WorkManager.initialize(this, wmConfig)

    // 2) Schedule your periodic sync (now guaranteed to use HiltWorkerFactory)
    episodesSyncScheduler.schedule(this)
  }

  override val workManagerConfiguration: Configuration
    get() = Configuration.Builder()
      .setWorkerFactory(workerFactory)
      .setMinimumLoggingLevel(
        if (BuildConfig.DEBUG) android.util.Log.DEBUG else android.util.Log.ERROR
      )
      .build()
}
