package top.contins.synapse.data.repository

import kotlinx.coroutines.flow.Flow
import top.contins.synapse.data.model.WritingContent

/**
 * 写作数据仓库接口
 */
interface WritingRepository {
    
    /**
     * 获取所有写作内容的Flow
     */
    val writings: Flow<List<WritingContent>>
    
    /**
     * 保存写作内容
     */
    suspend fun saveWriting(content: WritingContent): Boolean
    
    /**
     * 删除写作内容
     */
    suspend fun deleteWriting(id: String): Boolean
    
    /**
     * 获取所有写作内容
     */
    suspend fun getAllWritings(): List<WritingContent>
    
    /**
     * 根据ID获取写作内容
     */
    suspend fun getWritingById(id: String): WritingContent?
    
    /**
     * 发布写作内容
     */
    suspend fun publishWriting(content: WritingContent): Boolean
}