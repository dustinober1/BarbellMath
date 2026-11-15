package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.LoadingPlanDao
import com.example.data.dao.PlateInventoryDao
import com.example.data.dao.WorkoutSessionDao
import com.example.data.entity.LoadingPlanEntity
import com.example.data.entity.PlateInventoryEntity
import com.example.data.entity.WorkoutSessionEntity

@Database(
  entities = [LoadingPlanEntity::class, PlateInventoryEntity::class, WorkoutSessionEntity::class],
  version = 3,
  exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

  abstract fun loadingPlanDao(): LoadingPlanDao
  abstract fun plateInventoryDao(): PlateInventoryDao
  abstract fun workoutSessionDao(): WorkoutSessionDao

  companion object {
    @Volatile
    private var INSTANCE: AppDatabase? = null

    val MIGRATION_1_2 = object : Migration(1, 2) {
      override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
          """
          CREATE TABLE IF NOT EXISTS `plate_inventory` (
            `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
            `weight` REAL NOT NULL,
            `weightHundredths` INTEGER NOT NULL,
            `unit` TEXT NOT NULL,
            `colorArgb` INTEGER NOT NULL,
            `textColorArgb` INTEGER NOT NULL,
            `heightRatio` REAL NOT NULL,
            `thicknessFraction` REAL NOT NULL,
            `isEnabled` INTEGER NOT NULL,
            `availablePairs` INTEGER NOT NULL,
            `isCustom` INTEGER NOT NULL
          )
          """.trimIndent()
        )
        db.execSQL(
          "CREATE UNIQUE INDEX IF NOT EXISTS `index_plate_inventory_unit_weightHundredths` ON `plate_inventory` (`unit`, `weightHundredths`)"
        )
      }
    }

    val MIGRATION_2_3 = object : Migration(2, 3) {
      override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
          """
          CREATE TABLE IF NOT EXISTS `workout_sessions` (
            `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
            `timestamp` INTEGER NOT NULL,
            `liftName` TEXT NOT NULL,
            `reps` TEXT NOT NULL,
            `notes` TEXT NOT NULL,
            `targetWeight` REAL NOT NULL,
            `achievedWeight` REAL NOT NULL,
            `unit` TEXT NOT NULL,
            `barId` TEXT NOT NULL,
            `barName` TEXT NOT NULL,
            `barWeight` REAL NOT NULL,
            `customBarWeight` REAL NOT NULL,
            `collarsEnabled` INTEGER NOT NULL,
            `platesSummary` TEXT NOT NULL,
            `totalPlatesOnBar` INTEGER NOT NULL,
            `isExact` INTEGER NOT NULL
          )
          """.trimIndent()
        )
      }
    }

    fun getInstance(context: Context): AppDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          AppDatabase::class.java,
          "plate_master_database"
        )
          .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
          .fallbackToDestructiveMigration()
          .build()
        INSTANCE = instance
        instance
      }
    }
  }
}
