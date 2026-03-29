package com.example.slotanalyzer.data.datasource.seeder

import com.example.slotanalyzer.data.database.dao.MasterMetadataDao
import com.example.slotanalyzer.data.database.entity.MasterMetadataEntity
import javax.inject.Inject

class MachineMasterVersionStore @Inject constructor(
    private val dao: MasterMetadataDao
) {

    suspend fun getMasterVersion(): String? = dao.getByKey(KEY_MASTER_VERSION)?.value

    suspend fun getMasterVersionInt(): Int = getMasterVersion()?.toIntOrNull() ?: 0

    suspend fun saveMasterVersion(version: String, now: Long) {
        dao.upsert(
            MasterMetadataEntity(
                key = KEY_MASTER_VERSION,
                value = version,
                updatedAt = now
            )
        )
    }

    suspend fun saveMasterVersion(version: Int, now: Long) {
        saveMasterVersion(version.toString(), now)
    }

    suspend fun getLastCheckedAt(): Long = dao.getByKey(KEY_LAST_CHECKED_AT)?.value?.toLongOrNull() ?: 0L

    suspend fun saveLastCheckedAt(timestamp: Long) {
        dao.upsert(
            MasterMetadataEntity(
                key = KEY_LAST_CHECKED_AT,
                value = timestamp.toString(),
                updatedAt = timestamp
            )
        )
    }

    companion object {
        private const val KEY_MASTER_VERSION = "master_version"
        private const val KEY_LAST_CHECKED_AT = "master_last_checked_at"
    }
}
