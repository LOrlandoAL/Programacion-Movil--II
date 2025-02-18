package com.example.marsphotos

import android.app.Application
import com.example.marsphotos.data.MarsPhotosRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideRepository(application: Application) : MarsPhotosRepository
    {
        val application = (application as MarsPhotosApplication).container
        return application.marsPhotosRepository
    }
}