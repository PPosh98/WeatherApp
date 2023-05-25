package com.shah.myweatherapp.roomdb

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [WeatherEntity::class],
    version = 1,
    exportSchema = false
)

@TypeConverters(WeatherTypeConverter::class)
abstract class WeatherDatabase : RoomDatabase() {
    abstract fun weatherDAO() : WeatherDAO

    companion object {

        @Volatile
        private var instance: WeatherDatabase? = null

        fun getInstance(context: Context): WeatherDatabase {
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also { instance = it }
            }
        }

        private fun buildDatabase(context: Context) =
            Room.databaseBuilder(context, WeatherDatabase::class.java, "WeatherDatabase")
                .build()
    }
}