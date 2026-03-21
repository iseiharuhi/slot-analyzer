package com.example.slotanalyzer.data.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "play_histories",
    indices = [Index(value = ["played_date"]), Index(value = ["machine_id"])]
)
data class PlayHistoryEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "session_id") val sessionId: String,
    @ColumnInfo(name = "machine_id") val machineId: String,
    @ColumnInfo(name = "machine_name_snapshot") val machineNameSnapshot: String,
    @ColumnInfo(name = "played_date") val playedDate: Long,
    @ColumnInfo(name = "total_games") val totalGames: Int,
    @ColumnInfo(name = "big_count") val bigCount: Int,
    @ColumnInfo(name = "reg_count") val regCount: Int,
    @ColumnInfo(name = "cz_count") val czCount: Int,
    @ColumnInfo(name = "at_count") val atCount: Int,
    @ColumnInfo(name = "inference_summary") val inferenceSummary: String,
    @ColumnInfo(name = "confidence_label") val confidenceLabel: String,
    @ColumnInfo(name = "setting_scores") val settingScores: String,
    @ColumnInfo(name = "memo") val memo: String? = null,
    @ColumnInfo(name = "created_at") val createdAt: Long
)