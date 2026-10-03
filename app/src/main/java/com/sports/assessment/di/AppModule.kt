package com.sports.assessment.di

import com.sports.assessment.data.remote.ScheduleJson
import com.sports.assessment.data.remote.api.ScheduleApi
import com.sports.assessment.data.repository.ScheduleRepositoryImpl
import com.sports.assessment.domain.repository.ScheduleRepository
import com.sports.assessment.presentation.schedule.ScheduleViewModel
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.core.module.dsl.*
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.time.Clock

val appModule = module {
    // Time source for the "Next game" countdown and the Upcoming filter (tests use fixed clocks).
    single<Clock> { Clock.systemDefaultZone() }

    // Network
    single {
        val contentType = "application/json".toMediaType()

        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()

        Retrofit.Builder()
            .baseUrl(ScheduleApi.BASE_URL)
            .client(client)
            .addConverterFactory(ScheduleJson.asConverterFactory(contentType))
            .build()
            .create(ScheduleApi::class.java)
    }

    // Repository
    singleOf(::ScheduleRepositoryImpl) { bind<ScheduleRepository>() }

    // ViewModel (SavedStateHandle is supplied by Koin from the ViewModel's creation extras)
    viewModelOf(::ScheduleViewModel)
}
