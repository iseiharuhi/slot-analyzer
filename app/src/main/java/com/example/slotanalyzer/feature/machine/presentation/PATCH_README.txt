// ONLY PATCH PART

// replace this block in your existing ViewModel

val deduplicated = filtered
    .groupBy { machine -> normalizeMachineKey(machine.name) }
    .values
    .map { candidates ->
        candidates.minWithOrNull(machineDisplayComparator()) ?: candidates.first()
    }
    .sortedWith(machineComparator(preferences.machineSortOrder))
