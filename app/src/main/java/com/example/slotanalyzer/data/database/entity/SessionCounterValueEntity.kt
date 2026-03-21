package com.example.slotanalyzer.data.database.entity

import androidx.room.*

@Entity(
    tableName = "session_counter_values",
    foreignKeys = [
        ForeignKey(
            entity = PlaySessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["session_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["session_id"]), Index(value = ["session_id", "counter_key"], unique = true)]
)
data class SessionCounterValueEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "session_id") val sessionId: String,
    @ColumnInfo(name = "counter_key") val counterKey: String,
    @ColumnInfo(name = "int_value") val intValue: Int? = null,
    @ColumnInfo(name = "double_value") val doubleValue: Double? = null,
    @ColumnInfo(name = "updated_at") val updatedAt: Long
)
