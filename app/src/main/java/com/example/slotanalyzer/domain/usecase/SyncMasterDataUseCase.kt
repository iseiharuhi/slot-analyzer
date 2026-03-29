package com.example.slotanalyzer.domain.usecase

import com.example.slotanalyzer.domain.repository.MasterRepository
import com.example.slotanalyzer.core.datastore.MasterPreference
import javax.inject.Inject

class SyncMasterDataUseCase @Inject constructor(
    private val repository: MasterRepository,
    private val preference: MasterPreference
) {

    companion object {
        private const val CHECK_INTERVAL = 24 * 60 * 60 * 1000L
    }

    suspend operator fun invoke() {

        val now = System.currentTimeMillis()
        val lastChecked = preference.getLastChecked()

        if (now - lastChecked < CHECK_INTERVAL) return

        val remoteVersion = repository.fetchVersion()
        val localVersion = preference.getVersion()

        if (remoteVersion.version <= localVersion) {
            preference.setLastChecked(now)
            return
        }

        if (localVersion < remoteVersion.oldestAvailableDiffVersion) {
            fullSync(remoteVersion.version)
            preference.setLastChecked(now)
            return
        }

        diffSync(localVersion, remoteVersion.version)

        preference.setVersion(remoteVersion.version)
        preference.setLastChecked(now)
    }

    private suspend fun diffSync(localVersion: Int, latestVersion: Int) {

        var current = localVersion

        while (current < latestVersion) {

            val diff = repository.fetchDiff(current)

            val addedDetails = diff.added.map {
                repository.fetchMachineDetail(it.id)
            }

            val updatedDetails = diff.updated.map {
                repository.fetchMachineDetail(it.id)
            }

            repository.applyDiff(
                added = addedDetails,
                updated = updatedDetails,
                deleted = diff.deleted
            )

            current = diff.toVersion
        }
    }

    private suspend fun fullSync(latestVersion: Int) {
        // TODO implement full sync
    }
}
