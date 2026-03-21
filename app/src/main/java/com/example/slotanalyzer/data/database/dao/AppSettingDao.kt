package com.example.slotanalyzer.data.database.dao

import androidx.room.*
import com.example.slotanalyzer.data.database.entity.AppSettingEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AppSettingDao {
    @Query("SELECT * FROM app_settings")
    fun observeSettings(): Flow<List<AppSettingEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: AppSettingEntity)
}
