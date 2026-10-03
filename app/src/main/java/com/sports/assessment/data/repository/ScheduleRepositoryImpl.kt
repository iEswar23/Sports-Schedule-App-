package com.sports.assessment.data.repository

import com.sports.assessment.data.mapper.toDomain
import com.sports.assessment.data.remote.api.ScheduleApi
import com.sports.assessment.domain.model.ScheduleDomain
import com.sports.assessment.domain.repository.ScheduleRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class ScheduleRepositoryImpl(
    private val api: ScheduleApi
) : ScheduleRepository {

    override fun getSchedule(): Flow<Result<ScheduleDomain>> = flow {
        val result = try {
            Result.success(api.getSchedule().toDomain())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
        emit(result)
    }
}
