package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.LoadingPlanDao
import com.example.data.dao.PlateInventoryDao
import com.example.data.entity.LoadingPlanEntity
import com.example.data.entity.PlateInventoryEntity

@Database(
  entities = [LoadingPlanEntity::class, PlateInventoryEntity::class],
  version = 2,
  exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

  abstract fun loadingPlanDao(): LoadingPlanDao
  abstract fun plateInventoryDao(): PlateInventoryDao

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

    fun getInstance(context: Context): AppDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          AppDatabase::class.java,
          "plate_master_database"
        )
          .addMigrations(MIGRATION_1_2)
          .fallbackToDestructiveMigration()
          .build()
        INSTANCE = instance
        instance
      }
    }
  }
}
