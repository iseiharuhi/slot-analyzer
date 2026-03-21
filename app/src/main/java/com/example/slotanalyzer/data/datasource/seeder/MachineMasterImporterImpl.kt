package com.example.slotanalyzer.data.datasource.seeder

import androidx.room.withTransaction
import com.example.slotanalyzer.data.database.AppDatabase
import com.example.slotanalyzer.data.database.dao.MachineDao
import com.example.slotanalyzer.data.datasource.mapper.MachineMasterJsonMapper
import com.example.slotanalyzer.data.datasource.model.MachineMasterJson
import javax.inject.Inject

class MachineMasterImporterImpl @Inject constructor(
    private val database: AppDatabase,
    private val machineDao: MachineDao,
    private val mapper: MachineMasterJsonMapper
) : MachineMasterImporter {

    override suspend fun importMachine(master: MachineMasterJson, now: Long) {
        val machine = mapper.toMachineEntity(master.machine, now)
        val counters = mapper.toCounterEntities(master.machine.id, master.counterDefinitions)
        val refs = mapper.toReferenceEntities(master.machine.id, master.settingReferenceValues)
        val ceilings = mapper.toCeilingEntities(master.machine.id, master.ceilingRules)

        database.withTransaction {
            machineDao.deleteCounterDefinitionsByMachineId(master.machine.id)
            machineDao.deleteSettingReferenceValuesByMachineId(master.machine.id)
            machineDao.deleteCeilingRulesByMachineId(master.machine.id)
            machineDao.deleteMachineById(master.machine.id)

            machineDao.insertMachines(listOf(machine))
            machineDao.insertCounterDefinitions(counters)
            machineDao.insertSettingReferenceValues(refs)
            machineDao.insertCeilingRules(ceilings)
        }
    }
}
