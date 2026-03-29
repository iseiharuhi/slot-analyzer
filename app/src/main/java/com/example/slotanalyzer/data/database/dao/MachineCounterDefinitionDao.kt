package com.example.slotanalyzer.data.database.dao

import androidx.room.*
import com.example.slotanalyzer.data.database.entity.MachineCounterDefinitionEntity

@Dao
interface MachineCounterDefinitionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<MachineCounterDefinitionEntity>)

    @Query("DELETE FROM machine_counter_definitions WHERE machine_id = :machineId")
    suspend fun deleteByMachineId(machineId: String)

    @Query("DELETE FROM machine_counter_definitions WHERE machine_id = :machineId")
    suspend fun deleteCounterDefinitionsByMachineId(machineId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCounterDefinitions(entities: List<MachineCounterDefinitionEntity>)

    @Query("DELETE FROM machine_counter_definitions")
    suspend fun deleteAll()
}