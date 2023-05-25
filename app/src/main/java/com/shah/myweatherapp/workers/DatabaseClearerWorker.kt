package com.shah.myweatherapp.workers

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.shah.myweatherapp.roomdb.WeatherDatabase

class DatabaseClearerWorker(context: Context, workerParameters: WorkerParameters
    ) : Worker(context, workerParameters){

    override fun doWork(): Result {
        return try {
            emptyDatabase()
            Log.i("work", "doWork: Success")
            Result.success()
        } catch (e: Exception) {
            Log.e("work", "doWork: $e")
            Result.failure()
        }
    }

    private fun emptyDatabase() {
        WeatherDatabase.getInstance(applicationContext).weatherDAO().delete()
    }
}