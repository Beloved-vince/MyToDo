package com.example.mytodo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.mytodo.ui.theme.MyToDoTheme
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyToDoTheme {
                TodoApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoApp() {
    var todos by remember { mutableStateOf(listOf<Todo>()) }
    var showDialog by remember { mutableStateOf(false) }
    val completedCount = todos.count { it.completed }
    val totalCount = todos.size

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "My Tasks",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                        if (totalCount > 0) {
                            Text(
                                "$completedCount of $totalCount completed",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        }
    ) { padding ->
        if (todos.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(100.dp),
                    tint = Color.Gray.copy(alpha = 0.3f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text("No tasks yet", style = MaterialTheme.typography.titleLarge)
                Text("Tap + to add a task")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(todos) { todo ->
                    TodoCard(
                        todo = todo,
                        onToggle = {
                            todos = todos.map {
                                if (it.id == todo.id) it.copy(completed = !it.completed)
                                else it
                            }
                        },
                        onDelete = {
                            todos = todos.filter { it.id != todo.id }
                        }
                    )
                }
            }
        }
    }

    if (showDialog) {
        var title by remember { mutableStateOf("") }
        var description by remember { mutableStateOf("") }
        var priority by remember { mutableStateOf(1) }

        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Add New Task") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Title") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )

                    Text("Priority", style = MaterialTheme.typography.labelMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Low" to 0, "Medium" to 1, "High" to 2).forEach { (label, value) ->
                            FilterChip(
                                selected = priority == value,
                                onClick = { priority = value },
                                label = { Text(label) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (title.isNotBlank()) {
                            todos = todos + Todo(
                                id = UUID.randomUUID().toString(),
                                title = title,
                                description = description,
                                priority = priority
                            )
                            showDialog = false
                        }
                    }
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun TodoCard(
    todo: Todo,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    val priorityColor = when (todo.priority) {
        2 -> Color(0xFFEF5350) // High - Red
        1 -> Color(0xFFFF9800) // Medium - Orange
        else -> Color(0xFF66BB6A) // Low - Green
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(48.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(priorityColor)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Checkbox(
                checked = todo.completed,
                onCheckedChange = { onToggle() }
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = todo.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    textDecoration = if (todo.completed) TextDecoration.LineThrough else null
                )

                if (todo.description.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = todo.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                        .format(Date(todo.createdAt)),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }

            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Delete") },
                        onClick = {
                            onDelete()
                            showMenu = false
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Delete, contentDescription = null)
                        }
                    )
                }
            }
        }
    }
}

data class Todo(
    val id: String,
    val title: String,
    val description: String = "",
    val completed: Boolean = false,
    val priority: Int = 1,
    val createdAt: Long = System.currentTimeMillis()
)