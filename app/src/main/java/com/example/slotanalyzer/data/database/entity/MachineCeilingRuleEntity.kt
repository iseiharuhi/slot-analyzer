package com.example.slotanalyzer.data.database.entity

import androidx.room.*

@Entity(
    tableName = "machine_ceiling_rules",
    foreignKeys = [
        ForeignKey(
            entity = MachineEntity::class,
            parentColumns = ["id"],
            childColumns = ["machine_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["machine_id"]), Index(value = ["machine_id", "rule_key"], unique = true)]
)
data class MachineCeilingRuleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "machine_id") val machineId: String,
    @ColumnInfo(name = "rule_key") val ruleKey: String,
    @ColumnInfo(name = "display_name") val displayName: String,
    @ColumnInfo(name = "ceiling_type") val ceilingType: String,
    @ColumnInfo(name = "limit_value") val limitValue: Int,
    @ColumnInfo(name = "unit") val unit: String,
    @ColumnInfo(name = "reset_on_hit") val resetOnHit: Boolean = true,
    @ColumnInfo(name = "requires_reset_flag") val requiresResetFlag: Boolean = false,
    @ColumnInfo(name = "description") val description: String? = null,
    @ColumnInfo(name = "is_enabled") val isEnabled: Boolean = true
)
