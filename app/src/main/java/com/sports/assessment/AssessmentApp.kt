package com.sports.assessment

import android.app.Application
import com.sports.assessment.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class AssessmentApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@AssessmentApp)
            modules(appModule)
        }
    }
}
