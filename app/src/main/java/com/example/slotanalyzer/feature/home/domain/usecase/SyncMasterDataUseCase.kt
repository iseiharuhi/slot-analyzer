package com.example.slotanalyzer.feature.home.domain.usecase

import com.example.slotanalyzer.data.datasource.seeder.MachineMasterVersionStore
import com.example.slotanalyzer.domain.repository.MasterSyncRepository
import javax.inject.Inject

class SyncMasterDataUseCase @Inject constructor(
    private val repository: MasterSyncRepository,
    private val versionStore: MachineMasterVersionStore
) {

    suspend operator fun invoke() {
        val now = System.currentTimeMillis()
        val lastCheckedAt = versionStore.getLastCheckedAt()

        if (now - lastCheckedAt < CHECK_INTERVAL_MILLIS) {
            return
        }

        try {
            val remoteVersion = repository.fetchVersion()
            val localVersion = versionStore.getMasterVersionInt()

            if (remoteVersion.version <= localVersion) {
                versionStore.saveLastCheckedAt(now)
                return
            }

            if (localVersion < remoteVersion.oldestAvailableDiffVersion) {
                fullSync(now)
            } else {
                diffSync(localVersion, remoteVersion.version, now)
            }

            versionStore.saveMasterVersion(remoteVersion.version, now)
            versionStore.saveLastCheckedAt(now)

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private suspend fun diffSync(localVersion: Int, latestVersion: Int, now: Long) {
        var currentVersion = localVersion

        while (currentVersion < latestVersion) {
            val diff = repository.fetchDiff(currentVersion)

            diff.added.forEach { item ->
                repository.upsertMachineDetail(
                    repository.fetchMachineDetail(item.id),
                    now
                )
            }

            diff.updated.forEach { item ->
                repository.upsertMachineDetail(
                    repository.fetchMachineDetail(item.id),
                    now
                )
            }

            if (diff.deleted.isNotEmpty()) {
                repository.deleteMachines(diff.deleted)
            }

            currentVersion = diff.toVersion
        }
    }

    private suspend fun fullSync(now: Long) {
        val index = repository.fetchMachinesIndex()
        val details = index.machines.map { item ->
            repository.fetchMachineDetail(item.id)
        }
        repository.replaceAll(details, now)
    }

    companion object {
        private const val CHECK_INTERVAL_MILLIS = 24L * 60L * 60L * 1000L
    }
}