package top.contins.synapse.data.model

import java.util.*

data class WritingContent(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "",
    val content: String = "",
    val template: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isPublished: Boolean = false
)