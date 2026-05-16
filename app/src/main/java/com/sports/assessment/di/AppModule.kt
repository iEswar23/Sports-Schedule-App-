package com.sports.assessment.di

import com.sports.assessment.data.remote.api.ScheduleApi
import com.sports.assessment.data.repository.ScheduleRepositoryImpl
import com.sports.assessment.domain.repository.ScheduleRepository
import com.sports.assessment.presentation.schedule.ScheduleViewModel
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.core.module.dsl.*
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

val appModule = module {
    // Network
    single {
        val json = Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
        }
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
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
            .create(ScheduleApi::class.java)
    }

    // Repository
    singleOf(::ScheduleRepositoryImpl) { bind<ScheduleRepository>() }

    // ViewModel
    viewModelOf(::ScheduleViewModel)
}
