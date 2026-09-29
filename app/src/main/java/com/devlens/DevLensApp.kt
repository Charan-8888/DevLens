package com.devlens

import android.app.Application
import com.devlens.data.AppDatabase

class DevLensApp : Application() {
    lateinit var database: AppDatabase
        private set

    override fun onCreate() {
        super.onCreate()
        database = AppDatabase.getInstance(this)
    }
}
