package top.contins.synapse.ui.screens.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 写作页面 - 创作中心
 * 未来整合日程/待办功能
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview
fun WritingScreen() {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("创作", "草稿", "已发布")
    
    // 模拟创作数据
    val mockDrafts = listOf(
        DraftItem("AI技术发展趋势", "探讨人工智能在各个领域的应用前景...", "今天 14:30", false),
        DraftItem("高效学习方法", "分享一些提高学习效率的实用技巧...", "昨天 16:45", true),
        DraftItem("职场沟通艺术", "如何在工作中进行有效的沟通...", "2天前", false),
        DraftItem("时间管理心得", "个人时间管理的一些经验分享", "3天前", true)
    )

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // 顶部标题栏
        TopAppBar(
            title = {
                Text(
                    text = "📝 写作",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            actions = {
                IconButton(onClick = { }) {
                    Icon(Icons.Default.Settings, contentDescription = "设置")
                }
            }
        )

        // Tab栏
        TabRow(
            selectedTabIndex = selectedTab,
            modifier = Modifier.fillMaxWidth()
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) }
                )
            }
        }

        when (selectedTab) {
            0 -> CreateTab()
            1 -> DraftTab(mockDrafts.filter { !it.isPublished })
            2 -> PublishedTab(mockDrafts.filter { it.isPublished })
        }
    }
}

@Composable
fun CreateTab() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 快速创作按钮
        ElevatedButton(
            onClick = { },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.elevatedButtonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("开始新的创作", fontSize = 16.sp)
        }

        // 创作模板
        Text(
            text = "创作模板",
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium
        )
        
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(getWritingTemplates()) { template ->
                TemplateCard(template = template)
            }
        }
    }
}

@Composable
fun DraftTab(drafts: List<DraftItem>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(drafts) { draft ->
            DraftCard(draft = draft)
        }
    }
}

@Composable
fun PublishedTab(published: List<DraftItem>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(published) { item ->
            PublishedCard(item = item)
        }
    }
}

data class DraftItem(
    val title: String,
    val preview: String,
    val time: String,
    val isPublished: Boolean
)

data class WritingTemplate(
    val title: String,
    val description: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

fun getWritingTemplates() = listOf(
    WritingTemplate("日记", "记录每日心情和感悟", Icons.Default.Book),
    WritingTemplate("工作总结", "专业的工作总结模板", Icons.Default.Work),
    WritingTemplate("学习笔记", "整理学习内容和知识点", Icons.Default.School),
    WritingTemplate("项目计划", "制定详细的项目执行计划", Icons.Default.Assignment),
    WritingTemplate("创意文案", "营销和宣传文案创作", Icons.Default.Lightbulb)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemplateCard(template: WritingTemplate) {
    Card(
        onClick = { },
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                template.icon,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = template.title,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = template.description,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DraftCard(draft: DraftItem) {
    Card(
        onClick = { },
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = draft.title,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "更多")
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = draft.preview,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = draft.time,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PublishedCard(item: DraftItem) {
    Card(
        onClick = { },
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.title,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = "已发布",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                IconButton(onClick = { }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "更多")
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = item.preview,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = item.time,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row {
                    Text(
                        text = "👍 12",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "💬 5",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}