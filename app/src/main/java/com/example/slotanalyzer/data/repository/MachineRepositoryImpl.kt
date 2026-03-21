package com.example.slotanalyzer.data.repository

import com.example.slotanalyzer.data.database.dao.MachineDao
import com.example.slotanalyzer.data.datasource.seeder.MachineMasterSeeder
import com.example.slotanalyzer.data.mapper.MachineEntityMapper
import com.example.slotanalyzer.feature.machine.domain.model.Machine
import com.example.slotanalyzer.domain.repository.MachineRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class MachineRepositoryImpl @Inject constructor(
    private val dao: MachineDao,
    private val mapper: MachineEntityMapper,
    private val seeder: MachineMasterSeeder
) : MachineRepository {

    override fun observeActiveMachines(): Flow<List<Machine>> {
        return dao.observeActiveMachines().map { list ->
            list.mapNotNull { entity ->
                dao.getMachineWithDefinitions(entity.id)?.let { mapper.toDomain(it) }
            }
        }
    }

    override suspend fun getMachine(machineId: String): Machine? {
        return dao.getMachineWithDefinitions(machineId)?.let(mapper::toDomain)
    }

    override suspend fun seedIfNeeded() {
        seeder.seedIfNeeded()
    }
}
