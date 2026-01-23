package top.contins.synapse.feature.assistant

import androidx.compose.foundation.background
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import top.contins.synapse.core.ui.compose.markdown.MarkdownMessageItem
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import top.contins.synapse.domain.model.chat.Message
import top.contins.synapse.domain.model.chat.Conversation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    viewModel: ChatViewModel = hiltViewModel()
) {
    val messages by viewModel.messages.collectAsState()
    val inputText by viewModel.inputText.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val conversations by viewModel.conversations.collectAsState()
    val currentConversationId by viewModel.currentConversationId.collectAsState()

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    // Auto-scroll when messages change
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            delay(100) 
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    // Auto-scroll for streaming (check last message)
    val lastMessageTokenCount = messages.lastOrNull()?.content?.length ?: 0
    LaunchedEffect(lastMessageTokenCount) {
        val lastMsg = messages.lastOrNull()
        if (lastMsg?.isStreaming == true) {
             listState.animateScrollToItem(messages.size - 1)
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(280.dp),
                drawerContainerColor = MaterialTheme.colorScheme.surface,
            ) {
                // Header Area
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Icon(
                                imageVector = Icons.Default.SmartToy,
                                contentDescription = null,
                                modifier = Modifier.padding(8.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "聊天历史",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${conversations.size} 个对话",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                
                HorizontalDivider()

                // Conversation List
                LazyColumn(
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(conversations) { conversation ->
                        val isSelected = conversation.id == currentConversationId
                        val itemShape = RoundedCornerShape(16.dp)
                        Surface(
                            modifier = Modifier
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                .fillMaxWidth()
                                .clip(itemShape)
                                .clickable {
                                    viewModel.loadConversation(conversation)
                                    scope.launch { drawerState.close() }
                                },
                            color = if (isSelected) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            },
                            tonalElevation = if (isSelected) 2.dp else 0.dp,
                            shape = itemShape
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = if (isSelected) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.surface
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.SmartToy,
                                        contentDescription = null,
                                        modifier = Modifier.padding(6.dp),
                                        tint = if (isSelected) {
                                            MaterialTheme.colorScheme.onPrimary
                                        } else {
                                            MaterialTheme.colorScheme.primary
                                        }
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = conversation.title,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = if (isSelected) {
                                        MaterialTheme.colorScheme.onPrimaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.onSurface
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(
                                    onClick = { viewModel.deleteConversation(conversation.id) },
                                    colors = IconButtonDefaults.iconButtonColors(
                                        contentColor = MaterialTheme.colorScheme.error
                                    )
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete",
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    ) {
        Scaffold(
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            topBar = {
                TopAppBar(
                    title = { 
                        Text(
                            "这里是Syna~ 你的智能助手",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        ) 
                    },
                    windowInsets = WindowInsets(0, 0, 0, 0),
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    },
                    actions = {
                        IconButton(onClick = { viewModel.startNewChat() }) {
                            Icon(Icons.Default.Add, contentDescription = "New Chat")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface,
                        scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer
                    )
                )
            },
            bottomBar = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                ) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val inputShape = RoundedCornerShape(24.dp)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 44.dp, max = 140.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    shape = inputShape
                                )
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            BasicTextField(
                                value = inputText,
                                onValueChange = viewModel::updateInputText,
                                enabled = !isLoading,
                                textStyle = MaterialTheme.typography.bodyLarge.copy(
                                    color = MaterialTheme.colorScheme.onSurface
                                ),
                                modifier = Modifier.fillMaxWidth(),
                                decorationBox = { innerTextField ->
                                    if (inputText.isEmpty()) {
                                        Text(
                                            text = "今天过的怎么样~",
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    innerTextField()
                                }
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        IconButton(
                            onClick = viewModel::sendMessage,
                            enabled = !isLoading,
                            modifier = Modifier.size(44.dp)
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = MaterialTheme.colorScheme.primary,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Send,
                                    contentDescription = "Send",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                if (messages.isEmpty()) {
                    EmptyState()
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(messages) { message ->
                            ChatMessageBubble(message)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.SmartToy,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
            modifier = Modifier.size(120.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "咳咳，有什么想和我聊的吗？",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "不知道聊点什么好？不然让我跟你讲个故事吧！",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

@Composable
fun ChatMessageBubble(message: Message) {
    val isUser = message.role == Message.Role.USER
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isUser) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier.size(32.dp).align(Alignment.Top)
            ) {
                Icon(
                    imageVector = Icons.Default.SmartToy,
                    contentDescription = "AI",
                    modifier = Modifier.padding(6.dp),
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        BoxWithConstraints(
            modifier = Modifier.weight(1f, fill = false)
        ) {
            val bubbleMaxWidth = maxWidth * 0.78f

            Column(
                horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
            ) {
                Surface(
                    shape = RoundedCornerShape(
                        topStart = 20.dp,
                        topEnd = 20.dp,
                        bottomStart = if (isUser) 20.dp else 4.dp,
                        bottomEnd = if (isUser) 4.dp else 20.dp
                    ),
                    color = if (isUser) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.widthIn(max = bubbleMaxWidth)
                ) {
                    SelectionContainer {
                        Box(modifier = Modifier.padding(12.dp)) {
                            MarkdownMessageItem(
                                content = message.content,
                                isUser = isUser,
                                isStreaming = message.isStreaming
                            )
                        }
                    }
                }
            }
        }
        
        if (isUser) {
            Spacer(modifier = Modifier.width(8.dp))
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.tertiaryContainer,
                modifier = Modifier.size(32.dp).align(Alignment.Top)
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "User",
                    modifier = Modifier.padding(6.dp),
                    tint = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
        }
    }
}
