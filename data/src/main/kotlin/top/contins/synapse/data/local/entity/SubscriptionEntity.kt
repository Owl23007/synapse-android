package top.contins.synapse.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "subscriptions")
data class SubscriptionEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val url: String,
    val color: Long?,
    @ColumnInfo(name = "sync_interval", defaultValue = "24")
    val syncInterval: Int = 24,
    @ColumnInfo(name = "last_sync_at")
    val lastSyncAt: Long? = null,
    @ColumnInfo(name = "is_enabled", defaultValue = "1")
    val isEnabled: Boolean = true,
    @ColumnInfo(name = "created_at")
    val createdAt: Long
)
