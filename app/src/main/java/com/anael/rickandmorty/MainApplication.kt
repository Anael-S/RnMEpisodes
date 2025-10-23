package com.anael.rickandmorty

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.WorkManager
import com.anael.rickandmorty.infrastructure.work.EpisodesSyncScheduler
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class MainApplication : Application(), Configuration.Provider {

  @Inject lateinit var workerFactory: HiltWorkerFactory
  @Inject lateinit var episodesSyncScheduler: EpisodesSyncScheduler

  override fun onCreate() {
    super.onCreate()

    val wmConfig = Configuration.Builder()
      .setWorkerFactory(workerFactory)
      .setMinimumLoggingLevel(
        if (BuildConfig.DEBUG) android.util.Log.DEBUG else android.util.Log.ERROR
      )
      .build()
    WorkManager.initialize(this, wmConfig)

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
