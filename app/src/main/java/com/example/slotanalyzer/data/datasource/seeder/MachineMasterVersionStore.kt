package com.example.slotanalyzer.data.datasource.seeder

import com.example.slotanalyzer.data.database.dao.MasterMetadataDao
import com.example.slotanalyzer.data.database.entity.MasterMetadataEntity
import javax.inject.Inject

class MachineMasterVersionStore @Inject constructor(
    private val dao: MasterMetadataDao
) {
    suspend fun getMasterVersion(): String? = dao.getByKey("master_version")?.value

    suspend fun saveMasterVersion(version: String, now: Long) {
        dao.upsert(
            MasterMetadataEntity(
                key = "master_version",
                value = version,
                updatedAt = now
            )
        )
    }
}
