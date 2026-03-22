package com.example.slotanalyzer.data.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "play_sessions",
    indices = [
        Index(value = ["updated_at"]),
        Index(value = ["is_current"]),
        Index(value = ["is_archived_in_history"])
    ]
)
data class PlaySessionEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "machine_id") val machineId: String,
    @ColumnInfo(name = "machine_name_snapshot") val machineNameSnapshot: String,
    @ColumnInfo(name = "title") val title: String? = null,
    @ColumnInfo(name = "started_at") val startedAt: Long,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
    @ColumnInfo(name = "ended_at") val endedAt: Long? = null,
    @ColumnInfo(name = "memo") val memo: String? = null,
    @ColumnInfo(name = "is_finished") val isFinished: Boolean = false,
    @ColumnInfo(name = "is_current") val isCurrent: Boolean = true,
    @ColumnInfo(name = "is_archived_in_history") val isArchivedInHistory: Boolean = false,
    @ColumnInfo(name = "last_inference_summary") val lastInferenceSummary: String? = null,
    @ColumnInfo(name = "last_confidence_label") val lastConfidenceLabel: String? = null
)
