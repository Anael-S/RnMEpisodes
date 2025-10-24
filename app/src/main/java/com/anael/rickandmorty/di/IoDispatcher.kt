package com.anael.rickandmorty.di

import javax.inject.Qualifier

/**
 * DI - It’s used to tell Hilt/Dagger which specific dependency to inject
 * in this case, which CoroutineDispatchers : we use IoDispatcher
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class IoDispatcher
