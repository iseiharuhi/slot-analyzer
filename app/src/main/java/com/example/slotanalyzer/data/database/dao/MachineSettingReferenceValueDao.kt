package com.example.slotanalyzer.data.database.dao

import androidx.room.*
import com.example.slotanalyzer.data.database.entity.MachineSettingReferenceValueEntity

@Dao
interface MachineSettingReferenceValueDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<MachineSettingReferenceValueEntity>)

    @Query("DELETE FROM machine_setting_reference_values WHERE machine_id = :machineId")
    suspend fun deleteByMachineId(machineId: String)

    @Query("DELETE FROM machine_setting_reference_values WHERE machine_id = :machineId")
    suspend fun deleteSettingReferenceValuesByMachineId(machineId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSettingReferenceValues(entities: List<MachineSettingReferenceValueEntity>)

    @Query("DELETE FROM machine_setting_reference_values")
    suspend fun deleteAll()
}