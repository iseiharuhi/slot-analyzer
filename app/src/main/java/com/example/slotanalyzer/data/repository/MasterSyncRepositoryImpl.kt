package com.example.slotanalyzer.data.repository

import com.example.slotanalyzer.data.database.dao.*
import com.example.slotanalyzer.data.mapper.MasterRemoteMapper
import com.example.slotanalyzer.data.remote.api.MasterApi
import com.example.slotanalyzer.data.remote.dto.*
import com.example.slotanalyzer.domain.repository.MasterSyncRepository
import javax.inject.Inject

class MasterSyncRepositoryImpl @Inject constructor(
    private val api: MasterApi,
    private val machineDao: MachineDao,
    private val counterDao: MachineCounterDefinitionDao,
    private val referenceDao: MachineSettingReferenceValueDao,
    private val ceilingDao: MachineCeilingRuleDao,
    private val mapper: MasterRemoteMapper
) : MasterSyncRepository {

    override suspend fun fetchVersion(): VersionDto {
        return api.getVersion()
    }

    override suspend fun fetchDiff(fromVersion: Int): DiffDto {
        return api.getDiff(fromVersion)
    }

    override suspend fun fetchMachinesIndex(): MachinesIndexDto {
        return api.getMachines()
    }

    override suspend fun fetchMachineDetail(machineId: String): MachineDetailDto {
        return api.getMachineDetail(machineId)
    }

    override suspend fun upsertMachineDetail(detail: MachineDetailDto, now: Long) {
        val machine = mapper.toMachineEntity(detail, now)
        val counters = mapper.toCounterEntities(detail)
        val references = mapper.toReferenceEntities(detail)
        val ceilings = mapper.toCeilingEntities(detail)

        machineDao.insertOrReplace(machine)

        counterDao.deleteByMachineId(detail.id)
        referenceDao.deleteByMachineId(detail.id)
        ceilingDao.deleteByMachineId(detail.id)

        counterDao.insertAll(counters)
        referenceDao.insertAll(references)
        ceilingDao.insertAll(ceilings)
    }

    override suspend fun deleteMachines(ids: List<String>) {
        machineDao.deleteByIds(ids)
    }

    override suspend fun replaceAll(details: List<MachineDetailDto>, now: Long) {
        machineDao.deleteAll()
        counterDao.deleteAll()
        referenceDao.deleteAll()
        ceilingDao.deleteAll()

        details.forEach { detail ->
            upsertMachineDetail(detail, now)
        }
    }
}