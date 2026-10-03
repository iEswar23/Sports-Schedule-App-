package com.sports.assessment.data.remote

import kotlinx.serialization.json.Json

/** JSON configuration for the schedule feed, shared by Retrofit and the parsing tests. */
val ScheduleJson: Json = Json {
    ignoreUnknownKeys = true
    coerceInputValues = true
}
