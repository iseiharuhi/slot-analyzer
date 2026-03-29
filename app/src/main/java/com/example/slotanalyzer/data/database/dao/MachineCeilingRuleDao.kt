package com.example.slotanalyzer.data.database.dao

import androidx.room.*
import com.example.slotanalyzer.data.database.entity.MachineCeilingRuleEntity

@Dao
interface MachineCeilingRuleDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<MachineCeilingRuleEntity>)

    @Query("DELETE FROM machine_ceiling_rules WHERE machine_id = :machineId")
    suspend fun deleteByMachineId(machineId: String)

    @Query("DELETE FROM machine_ceiling_rules WHERE machine_id = :machineId")
    suspend fun deleteCeilingRulesByMachineId(machineId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCeilingRules(entities: List<MachineCeilingRuleEntity>)

    @Query("DELETE FROM machine_ceiling_rules")
    suspend fun deleteAll()
}