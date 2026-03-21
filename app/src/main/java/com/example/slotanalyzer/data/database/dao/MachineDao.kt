package com.example.slotanalyzer.data.database.dao

import androidx.room.*
import com.example.slotanalyzer.data.database.entity.*
import com.example.slotanalyzer.data.database.relation.MachineWithDefinitions
import kotlinx.coroutines.flow.Flow

@Dao
interface MachineDao {
    @Query("SELECT * FROM machines WHERE is_active = 1 ORDER BY name ASC")
    fun observeActiveMachines(): Flow<List<MachineEntity>>

    @Transaction
    @Query("SELECT * FROM machines WHERE id = :machineId LIMIT 1")
    suspend fun getMachineWithDefinitions(machineId: String): MachineWithDefinitions?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMachines(items: List<MachineEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCounterDefinitions(items: List<MachineCounterDefinitionEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSettingReferenceValues(items: List<MachineSettingReferenceValueEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCeilingRules(items: List<MachineCeilingRuleEntity>)

    @Query("DELETE FROM machine_counter_definitions WHERE machine_id = :machineId")
    suspend fun deleteCounterDefinitionsByMachineId(machineId: String)

    @Query("DELETE FROM machine_setting_reference_values WHERE machine_id = :machineId")
    suspend fun deleteSettingReferenceValuesByMachineId(machineId: String)

    @Query("DELETE FROM machine_ceiling_rules WHERE machine_id = :machineId")
    suspend fun deleteCeilingRulesByMachineId(machineId: String)

    @Query("DELETE FROM machines WHERE id = :machineId")
    suspend fun deleteMachineById(machineId: String)
}
