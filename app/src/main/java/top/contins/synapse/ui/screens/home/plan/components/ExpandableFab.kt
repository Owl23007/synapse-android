package top.contins.synapse.ui.screens.home.plan.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Task
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * 可展开的浮动操作按钮
 * 
 * 根据当前选中的 Tab，展示不同的操作按钮
 * 
 * @param expanded 是否展开
 * @param onExpandToggle 展开/收起回调
 * @param selectedTab 当前选中的 Tab 索引
 * @param onTaskAdd 添加任务回调
 * @param onScheduleAdd 添加日程回调
 * @param onGoalAdd 添加目标回调
 */
@Composable
fun ExpandableFab(
    expanded: Boolean,
    onExpandToggle: () -> Unit,
    selectedTab: Int,
    onTaskAdd: () -> Unit,
    onScheduleAdd: () -> Unit,
    onGoalAdd: () -> Unit
) {
    val rotation by animateFloatAsState(
        targetValue = if (expanded) 45f else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "rotation"
    )
    
    val fabColor by animateColorAsState(
        targetValue = if (expanded) Color.White else MaterialTheme.colorScheme.primary,
        animationSpec = tween(durationMillis = 300),
        label = "fabColor"
    )
    
    val iconColor by animateColorAsState(
        targetValue = if (expanded) MaterialTheme.colorScheme.primary else Color.White,
        animationSpec = tween(durationMillis = 300),
        label = "iconColor"
    )
    
    Column(
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 小FAB按钮（根据当前tab显示不同选项）
        if (selectedTab == 0) {
            // 今日tab：显示任务和日程按钮
            MiniFab(
                icon = Icons.Filled.Event,
                text = "日程",
                onClick = onScheduleAdd,
                expanded = expanded
            )
            MiniFab(
                icon = Icons.Filled.Task,
                text = "任务",
                onClick = onTaskAdd,
                expanded = expanded
            )
        } else {
            // 其他tab：显示单个对应按钮
            val (icon, text, onClick) = when (selectedTab) {
                1 -> Triple(Icons.Filled.Event, "日程", onScheduleAdd)
                2 -> Triple(Icons.Filled.Task, "任务", onTaskAdd)
                3 -> Triple(Icons.Filled.Flag, "目标", onGoalAdd)
                else -> Triple(Icons.Filled.Add, "", {})
            }
            
            MiniFab(
                icon = icon,
                text = text,
                onClick = onClick,
                expanded = expanded
            )
        }
        
        // 主FAB
        FloatingActionButton(
            onClick = onExpandToggle,
            containerColor = fabColor,
            contentColor = iconColor
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = "添加",
                modifier = Modifier.graphicsLayer {
                    rotationZ = rotation
                }
            )
        }
    }
}

/**
 * 迷你浮动操作按钮
 * 
 * 用于展开 FAB 中的子操作
 * 
 * @param icon 按钮图标
 * @param text 按钮文本标签
 * @param onClick 点击回调
 * @param expanded 是否处于展开状态
 */
@Composable
fun MiniFab(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    onClick: () -> Unit,
    expanded: Boolean
) {
    val alpha by animateFloatAsState(
        targetValue = if (expanded) 1f else 0f,
        animationSpec = tween(durationMillis = 300),
        label = "alpha"
    )
    
    val scale by animateFloatAsState(
        targetValue = if (expanded) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "scale"
    )
    
    if (expanded) {
        FloatingActionButton(
            onClick = onClick,
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.graphicsLayer {
                this.alpha = alpha
                scaleX = scale
                scaleY = scale
            }
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = text,
                    modifier = Modifier.size(20.dp)
                )
                if (text.isNotEmpty()) {
                    Text(
                        text = text,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
