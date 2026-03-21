package com.example.slotanalyzer.data.database.dao

import androidx.room.*
import com.example.slotanalyzer.data.database.entity.MasterMetadataEntity

@Dao
interface MasterMetadataDao {
    @Query("SELECT * FROM master_metadata WHERE `key` = :key LIMIT 1")
    suspend fun getByKey(key: String): MasterMetadataEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: MasterMetadataEntity)
}
