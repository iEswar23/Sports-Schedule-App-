package com.sports.assessment.data.repository

import com.sports.assessment.data.mapper.toDomain
import com.sports.assessment.data.remote.api.ScheduleApi
import com.sports.assessment.domain.model.ScheduleDomain
import com.sports.assessment.domain.repository.ScheduleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.IOException

class ScheduleRepositoryImpl(
    private val api: ScheduleApi
) : ScheduleRepository {

    override fun getSchedule(): Flow<Result<ScheduleDomain>> = flow {
        try {
            val response = api.getSchedule()
            emit(Result.success(response.toDomain()))
        } catch (e: IOException) {
            emit(Result.failure(e))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
}
