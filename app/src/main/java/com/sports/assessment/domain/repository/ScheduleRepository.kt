package com.sports.assessment.domain.repository

import com.sports.assessment.domain.model.ScheduleDomain
import kotlinx.coroutines.flow.Flow

interface ScheduleRepository {
    fun getSchedule(): Flow<Result<ScheduleDomain>>
}
