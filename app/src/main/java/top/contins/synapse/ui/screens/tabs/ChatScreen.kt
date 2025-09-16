package top.contins.synapse.ui.screens.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 对话页面 - 专属评价/反馈/协作对话区
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview
fun ChatScreen() {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("聊天", "协作", "反馈")

    // 模拟对话数据
    val mockChats = listOf(
        ChatItem("AI助手", "您好！有什么可以帮助您的吗？", "刚刚", true, 0),
        ChatItem("写作小组", "大家觉得这个文章标题怎么样？", "5分钟前", false, 3),
        ChatItem("项目协作", "明天的会议准备好了吗？", "1小时前", false, 1),
        ChatItem("学习讨论", "关于AI学习路径的讨论", "2小时前", false, 0),
        ChatItem("产品反馈", "新功能体验如何？", "昨天", false, 2)
    )

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // 顶部标题栏
        TopAppBar(
            title = {
                Text(
                    text = "💬 对话",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            actions = {
                IconButton(onClick = { }) {
                    Icon(Icons.Default.Search, contentDescription = "搜索")
                }
                IconButton(onClick = { }) {
                    Icon(Icons.Default.Add, contentDescription = "新建对话")
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
            0 -> ChatListTab(mockChats)
            1 -> CollaborationTab()
            2 -> FeedbackTab()
        }
    }
}

@Composable
fun ChatListTab(chats: List<ChatItem>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        items(chats) { chat ->
            ChatItemCard(chat = chat)
        }
    }
}

@Composable
fun CollaborationTab() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "协作项目",
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(getCollaborationProjects()) { project ->
                CollaborationCard(project = project)
            }
        }
    }
}

@Composable
fun FeedbackTab() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "用户反馈",
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium
        )

        // 快速反馈按钮
        OutlinedButton(
            onClick = { },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Feedback, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("提交反馈")
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(getFeedbackItems()) { feedback ->
                FeedbackCard(feedback = feedback)
            }
        }
    }
}

data class ChatItem(
    val name: String,
    val lastMessage: String,
    val time: String,
    val isAI: Boolean,
    val unreadCount: Int
)

data class CollaborationProject(
    val title: String,
    val description: String,
    val members: Int,
    val status: String
)

data class FeedbackItem(
    val title: String,
    val content: String,
    val status: String,
    val time: String
)

fun getCollaborationProjects() = listOf(
    CollaborationProject("AI写作指南", "团队合作撰写AI使用指南", 5, "进行中"),
    CollaborationProject("产品优化方案", "讨论产品功能改进建议", 3, "待开始"),
    CollaborationProject("用户体验研究", "分析用户使用习惯和痛点", 7, "已完成")
)

fun getFeedbackItems() = listOf(
    FeedbackItem("界面优化建议", "希望能够支持深色模式", "已处理", "3天前"),
    FeedbackItem("功能建议", "增加语音输入功能", "处理中", "1周前"),
    FeedbackItem("问题反馈", "应用偶尔会闪退", "已修复", "2周前")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatItemCard(chat: ChatItem) {
    Card(
        onClick = { },
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 头像
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        if (chat.isAI) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.secondary
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (chat.isAI) Icons.Default.SmartToy else Icons.Default.Group,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // 内容
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = chat.name,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = chat.time,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = chat.lastMessage,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        modifier = Modifier.weight(1f)
                    )

                    if (chat.unreadCount > 0) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = chat.unreadCount.toString(),
                                    color = Color.White,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollaborationCard(project: CollaborationProject) {
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
                    text = project.title,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = when (project.status) {
                        "进行中" -> MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        "待开始" -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)
                        else -> Color.Green.copy(alpha = 0.1f)
                    }
                ) {
                    Text(
                        text = project.status,
                        fontSize = 10.sp,
                        color = when (project.status) {
                            "进行中" -> MaterialTheme.colorScheme.primary
                            "待开始" -> MaterialTheme.colorScheme.secondary
                            else -> Color.Green
                        },
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = project.description,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.People,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${project.members} 人参与",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedbackCard(feedback: FeedbackItem) {
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
                    text = feedback.title,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = when (feedback.status) {
                        "已处理" -> Color.Green.copy(alpha = 0.1f)
                        "处理中" -> MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        else -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)
                    }
                ) {
                    Text(
                        text = feedback.status,
                        fontSize = 10.sp,
                        color = when (feedback.status) {
                            "已处理" -> Color.Green
                            "处理中" -> MaterialTheme.colorScheme.primary
                            else -> MaterialTheme.colorScheme.secondary
                        },
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = feedback.content,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = feedback.time,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}