package top.contins.synapse.feature.task.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import top.contins.synapse.feature.task.viewmodel.TaskViewModel
import top.contins.synapse.domain.model.Task
import top.contins.synapse.domain.model.TaskStatus
import top.contins.synapse.domain.model.TaskPriority
import java.util.*

@Composable
fun TaskScreen() {
    val viewModel: TaskViewModel = viewModel()


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "任务管理",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // 添加任务按钮
        Button(
            onClick = {
                val newTask = Task(
                    UUID.randomUUID().toString(),
                    "示例任务",
                    "这是一个示例任务",
                    Date(),
                    TaskStatus.TODO,
                    TaskPriority.MEDIUM
                )
               // viewModel.addTask(newTask)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Text("添加示例任务")
        }

        // 任务列表
        LazyColumn {
            //items(tasks) { task ->
               // TaskItem(
                //    task = task,
               //     onTaskClick = { /* 处理任务点击 */ },
              //      onDeleteClick = { viewModel.deleteTask(task.id) }
              //  )
           // }
        }
    }
}

@Composable
fun TaskItem(
    task: Task,
    onTaskClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = task.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = task.description,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(vertical = 4.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${task.status.displayName} | ${task.priority.displayName}",
                    style = MaterialTheme.typography.bodySmall
                )
                Button(
                    onClick = onDeleteClick
                ) {
                    Text("删除")
                }
            }
        }
    }
}
