package top.contins.synapse.data.repository

import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import top.contins.synapse.data.model.WritingContent
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 写作数据仓库实现类
 * 目前使用内存存储，后续可以扩展为数据库存储
 */
@Singleton
class WritingRepositoryImpl @Inject constructor() : WritingRepository {
    
    private val _writings = MutableStateFlow<List<WritingContent>>(emptyList())
    override val writings: Flow<List<WritingContent>> = _writings.asStateFlow()
    
    private fun updateWritings(newList: List<WritingContent>) {
        _writings.value = newList
    }
    
    override suspend fun saveWriting(content: WritingContent): Boolean {
        return try {
            val currentList = _writings.value.toMutableList()
            val existingIndex = currentList.indexOfFirst { it.id == content.id }
            if (existingIndex >= 0) {
                currentList[existingIndex] = content
            } else {
                currentList.add(content)
            }
            updateWritings(currentList)
            Log.d("WritingRepository", "Writing saved: ${content.title}")
            true
        } catch (e: Exception) {
            Log.e("WritingRepository", "Failed to save writing", e)
            false
        }
    }
    
    override suspend fun deleteWriting(id: String): Boolean {
        return try {
            val currentList = _writings.value.toMutableList()
            val removed = currentList.removeIf { it.id == id }
            if (removed) {
                updateWritings(currentList)
            }
            Log.d("WritingRepository", "Writing deleted: $id, success: $removed")
            removed
        } catch (e: Exception) {
            Log.e("WritingRepository", "Failed to delete writing", e)
            false
        }
    }
    
    override suspend fun getAllWritings(): List<WritingContent> {
        return try {
            _writings.value.toList()
        } catch (e: Exception) {
            Log.e("WritingRepository", "Failed to get all writings", e)
            emptyList()
        }
    }
    
    override suspend fun getWritingById(id: String): WritingContent? {
        return try {
            _writings.value.find { it.id == id }
        } catch (e: Exception) {
            Log.e("WritingRepository", "Failed to get writing by id", e)
            null
        }
    }
    
    override suspend fun publishWriting(content: WritingContent): Boolean {
        return try {
            val publishedContent = content.copy(isPublished = true)
            saveWriting(publishedContent)
        } catch (e: Exception) {
            Log.e("WritingRepository", "Failed to publish writing", e)
            false
        }
    }
}