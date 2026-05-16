package com.sports.assessment.data.remote.api

import com.sports.assessment.data.remote.dto.ScheduleResponseDto
import retrofit2.http.GET

interface ScheduleApi {
    @GET("iOS/interviews/ScheduleExercise/schedule.json")
    suspend fun getSchedule(): ScheduleResponseDto

    companion object {
        const val BASE_URL = "http://files.yinzcam.com.s3.amazonaws.com/"
    }
}
