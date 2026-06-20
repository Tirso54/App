package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Task
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Calendar

// Theme colors specifically tailored for TIMEBLOCK PRO
val SlateDarkBg = Color(0xFF0F172A)      // Deep slate
val SlateCardBg = Color(0xFF1E293B)      // Card body
val BorderColor = Color(0xFF334155)      // Fine borders

val ColorTrabajo = Color(0xFF38BDF8)     // Blue
val ColorEstudio = Color(0xFFC084FC)     // Purple
val ColorDeporte = Color(0xFF34D399)     // Green
val ColorOcio = Color(0xFFFBBF24)        // Yellow
val ColorOverload = Color(0xFFEF4444)     // Crimson Red
val ColorPrimaryNeon = Color(0xFF6366F1)  // Indigo primary

fun getCategoryColor(category: String): Color {
    return when (category.lowercase().trim()) {
        "trabajo" -> ColorTrabajo
        "estudio" -> ColorEstudio
        "deporte" -> ColorDeporte
        else -> ColorOcio
    }
}

fun getCategoryIcon(category: String): ImageVector {
    return when (category.lowercase().trim()) {
        "trabajo" -> Icons.Default.Work
        "estudio" -> Icons.Default.School
        "deporte" -> Icons.Default.FitnessCenter
        else -> Icons.Default.Spa
    }
}

fun getDayName(day: Int): String {
    return when (day) {
        1 -> "Lunes"
        2 -> "Martes"
        3 -> "Miércoles"
        4 -> "Jueves"
        5 -> "Viernes"
        6 -> "Sábado"
        7 -> "Domingo"
        else -> "Día"
    }
}

fun getDayAbbrev(day: Int): String {
    return when (day) {
        1 -> "LUN"
        2 -> "MAR"
        3 -> "MIÉ"
        4 -> "JUE"
        5 -> "VIE"
        6 -> "SÁB"
        7 -> "DOM"
        else -> "D"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeBlockScreen(viewModel: TimeBlockViewModel) {
    val tasks by viewModel.tasksState.collectAsState()
    val optimization by viewModel.optimizationState.collectAsState()
    val scope = rememberCoroutineScope()

    var selectedTab by remember { mutableStateOf(0) } // 0 = Semanal, 1 = Modo Enfoque
    var selectedDay by remember { mutableStateOf(1) } // Default Monday (1)
    
    // Auto-select current real day for first screen launch
    LaunchedEffect(Unit) {
        val calendar = Calendar.getInstance()
        // Calendar Sunday is 1, Monday is 2
        val day = when (calendar.get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> 1
            Calendar.TUESDAY -> 2
            Calendar.WEDNESDAY -> 3
            Calendar.THURSDAY -> 4
            Calendar.FRIDAY -> 5
            Calendar.SATURDAY -> 6
            Calendar.SUNDAY -> 7
            else -> 1
        }
        selectedDay = day
    }

    var showAddTaskDialog by remember { mutableStateOf(false) }
    var taskToEdit by remember { mutableStateOf<Task?>(null) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = "TimeBlock Pro Logo",
                            tint = ColorOcio,
                            modifier = Modifier
                                .size(32.dp)
                                .padding(end = 4.dp)
                        )
                        Text(
                            text = "TIMEBLOCK PRO",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 2.sp
                            ),
                            color = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.loadDefaultOverloadedTasks() },
                        modifier = Modifier.testTag("reset_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Restablecer Datos Demo",
                            tint = Color.White.copy(alpha = 0.7f)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SlateDarkBg,
                    titleContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            if (selectedTab == 0) {
                FloatingActionButton(
                    onClick = {
                        taskToEdit = null
                        showAddTaskDialog = true
                    },
                    containerColor = ColorPrimaryNeon,
                    contentColor = Color.White,
                    modifier = Modifier.testTag("add_task_fab")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Añadir Bloque")
                }
            }
        },
        containerColor = SlateDarkBg
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(SlateDarkBg)
        ) {
            
            // Modern Custom Tab Slider (Semanal vs Modo Enfoque)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(SlateCardBg)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val tabTitles = listOf("Planificador Semanal", "Modo Enfoque 🎯")
                tabTitles.forEachIndexed { index, title ->
                    val isSelected = selectedTab == index
                    val animBg by animateColorAsState(if (isSelected) ColorPrimaryNeon else Color.Transparent)
                    val animText by animateColorAsState(if (isSelected) Color.White else Color.White.copy(alpha = 0.6f))
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(animBg)
                            .clickable { selectedTab = index }
                            .testTag(if (index == 0) "semanal_tab" else "enfoque_tab"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = animText
                        )
                    }
                }
            }

            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = {
                    slideInHorizontally { width -> if (targetState > initialState) width else -width } togetherWith
                            slideOutHorizontally { width -> if (targetState > initialState) -width else width }
                }
            ) { tab ->
                if (tab == 0) {
                    // WEEKLY PLANNER VIEW
                    Column(modifier = Modifier.fillMaxSize()) {
                        
                        // Overload global check & AI Banner
                        WeeklyOverloadAndAIBanner(
                            tasks = tasks,
                            onOptimizeClick = { viewModel.runAIOptimization() }
                        )

                        // 7 Days Horizontal Selector Row
                        ScrollableDaysBar(
                            tasks = tasks,
                            selectedDay = selectedDay,
                            onDaySelected = { selectedDay = it }
                        )

                        // Today detail or simple tasks reordering list
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        val dayTasks = tasks.filter { it.dayOfWeek == selectedDay }.sortedBy { it.orderIndex }
                        val dayTotalDuration = dayTasks.sumOf { it.durationHours }
                        val isDayOverloaded = dayTotalDuration > 8.0

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Bloques del ${getDayName(selectedDay)}",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White
                                )
                                Text(
                                    text = "Horas totales: ${dayTotalDuration}h",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (isDayOverloaded) ColorOverload else Color.White.copy(alpha = 0.8f)
                                    )
                                )
                            }
                            
                            if (isDayOverloaded) {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = ColorOverload.copy(alpha = 0.15f)),
                                    border = BoxBorder(1.dp, ColorOverload),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Warning,
                                            contentDescription = "Sobrecarga",
                                            tint = ColorOverload,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = "¡Día sobrecargado! ($dayTotalDuration h)",
                                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                                color = ColorOverload
                                            )
                                            Text(
                                                text = "Supera el límite de 8 horas. Considera usar la optimización de IA para equilibrar tu semana.",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Color.White.copy(alpha = 0.8f),
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            } else {
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }

                        // Tasks list
                        if (dayTasks.isEmpty()) {
                            EmptyStateBlock(onQuickAddTask = {
                                viewModel.addTask(
                                    title = "Bloque de Trabajo Nuevo",
                                    durationHours = 2.0,
                                    category = "trabajo",
                                    priority = "media",
                                    dayOfWeek = selectedDay
                                )
                            })
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                contentPadding = PaddingValues(16.dp, 8.dp, 16.dp, 80.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(dayTasks, key = { it.id }) { task ->
                                    TaskBlockCard(
                                        task = task,
                                        onToggleComplete = { viewModel.toggleTaskCompletion(task) },
                                        onEditClick = {
                                            taskToEdit = task
                                            showAddTaskDialog = true
                                        },
                                        onDeleteClick = { viewModel.deleteTaskById(task.id) },
                                        onMoveDayClick = { newDay -> viewModel.moveTaskToDay(task, newDay) },
                                        onShiftOrderClick = { up -> viewModel.shiftTaskOrder(task, up) }
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // FOCUS MODE VIEW (CURRENT DAY FOCUS)
                    ModoEnfoqueView(
                        tasks = tasks,
                        onToggleComplete = { viewModel.toggleTaskCompletion(it) }
                    )
                }
            }
        }

        // Add / Edit Task Dialog with customizable elements
        if (showAddTaskDialog) {
            AddEditTaskDialog(
                task = taskToEdit,
                dayOfWeek = selectedDay,
                onDismiss = { showAddTaskDialog = false },
                onSave = { title, duration, category, priority, day ->
                    if (taskToEdit == null) {
                        viewModel.addTask(title, duration, category, priority, day)
                    } else {
                        viewModel.updateTask(taskToEdit!!.copy(
                            title = title,
                            durationHours = duration,
                            category = category,
                            priority = priority,
                            dayOfWeek = day
                        ))
                    }
                    showAddTaskDialog = false
                }
            )
        }

        // Active optimization process UI BottomSheet / Dialog
        when (val optState = optimization) {
            is OptimizationState.Loading -> {
                AlertDialog(
                    onDismissRequest = {},
                    confirmButton = {},
                    title = {
                        Text(
                            text = "Optimizador IA ✨",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    },
                    text = {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp)
                        ) {
                            CircularProgressIndicator(color = ColorPrimaryNeon)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Nuestra Inteligencia Artificial está analizando tus bloques de trabajo, redistribuyendo las tareas para evitar sobrecargas y recomendándote descansos óptimos...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.8f),
                                textAlign = TextAlign.Center
                            )
                        }
                    },
                    containerColor = SlateCardBg,
                    shape = RoundedCornerShape(16.dp)
                )
            }
            is OptimizationState.Success -> {
                AlertDialog(
                    onDismissRequest = { viewModel.dismissOptimization() },
                    confirmButton = {
                        Button(
                            onClick = {
                                viewModel.applyOptimizedTasks(optState.optimizedTasks)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ColorPrimaryNeon)
                        ) {
                            Text("Aplicar Cambios", fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { viewModel.dismissOptimization() }) {
                            Text("Cancelar", color = Color.White.copy(alpha = 0.6f))
                        }
                    },
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "AI Success",
                                tint = ColorOcio,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "¡Planificación Optimizada!",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                        }
                    },
                    text = {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 320.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            item {
                                Text(
                                    text = "Propuesta de Reestructuración IA:",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = ColorOcio
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                            }
                            items(optState.suggestions) { suggestion ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = ColorPrimaryNeon.copy(alpha = 0.1f)),
                                    border = BoxBorder(1.dp, ColorPrimaryNeon.copy(alpha = 0.3f)),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.TipsAndUpdates,
                                            contentDescription = "Tip",
                                            tint = ColorOcio,
                                            modifier = Modifier
                                                .size(20.dp)
                                                .padding(top = 2.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = suggestion,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                            item {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "La IA distribuyó la carga pesada del Lunes para que no superes las 8 horas, y añadió descansos preventivos tras bloques clave.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.6f)
                                )
                            }
                        }
                    },
                    containerColor = SlateCardBg,
                    shape = RoundedCornerShape(16.dp)
                )
            }
            is OptimizationState.Error -> {
                AlertDialog(
                    onDismissRequest = { viewModel.dismissOptimization() },
                    confirmButton = {
                        Button(
                            onClick = { viewModel.dismissOptimization() },
                            colors = ButtonDefaults.buttonColors(containerColor = ColorPrimaryNeon)
                        ) {
                            Text("Entendido", fontWeight = FontWeight.Bold)
                        }
                    },
                    title = {
                        Text(
                            text = "Aviso del Optimizador",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    },
                    text = {
                        Text(
                            text = optState.message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    },
                    containerColor = SlateCardBg,
                    shape = RoundedCornerShape(16.dp)
                )
            }
            else -> {}
        }
    }
}

// UI helper block for showing total weekly status and inviting AI Optimisation
@Composable
fun WeeklyOverloadAndAIBanner(tasks: List<Task>, onOptimizeClick: () -> Unit) {
    // Check if any day has > 8 hours of tasks
    val dailyHours = (1..7).map { d ->
        tasks.filter { it.dayOfWeek == d }.sumOf { it.durationHours }
    }
    val overloadedDays = dailyHours.filter { it > 8.0 }.size
    val totalWeeklyHours = dailyHours.sum()

    Card(
        colors = CardDefaults.cardColors(containerColor = SlateCardBg),
        shape = RoundedCornerShape(16.dp),
        border = BoxBorder(1.dp, BorderColor),
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Balance Semanal",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                    Text(
                        text = "${totalWeeklyHours}h planificadas",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black),
                        color = Color.White
                    )
                }

                if (overloadedDays > 0) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(ColorOverload.copy(alpha = 0.15f))
                            .border(1.dp, ColorOverload, RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        Text(
                            text = "⚠️ $overloadedDays d sobrecargados",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            color = ColorOverload
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(ColorDeporte.copy(alpha = 0.15f))
                            .border(1.dp, ColorDeporte, RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        Text(
                            text = "✅ Todo Balanceado",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            color = ColorDeporte
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            
            // Neon glowing styled button to evoke modern AI power
            Button(
                onClick = onOptimizeClick,
                colors = ButtonDefaults.buttonColors(containerColor = ColorPrimaryNeon),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .testTag("ai_optimize_button"),
                contentPadding = PaddingValues(0.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "IA",
                        tint = ColorOcio,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Optimizar Semana con IA ✨",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                }
            }
        }
    }
}

// 7-day horizontal bar with a small red dot indicator if day is overloaded (> 8h)
@Composable
fun ScrollableDaysBar(tasks: List<Task>, selectedDay: Int, onDaySelected: (Int) -> Unit) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items((1..7).toList()) { dayVal ->
            val dayTasks = tasks.filter { it.dayOfWeek == dayVal }
            val dayTotalHours = dayTasks.sumOf { it.durationHours }
            val isOverloaded = dayTotalHours > 8.0
            val isSelected = selectedDay == dayVal

            val borderBrush = if (isSelected) {
                Brush.linearGradient(listOf(ColorPrimaryNeon, ColorEstudio))
            } else if (isOverloaded) {
                Brush.linearGradient(listOf(ColorOverload, ColorOverload))
            } else {
                Brush.linearGradient(listOf(BorderColor, BorderColor))
            }

            Box(
                modifier = Modifier
                    .width(64.dp)
                    .height(72.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (isSelected) ColorPrimaryNeon.copy(alpha = 0.15f) else SlateCardBg)
                    .clickable { onDaySelected(dayVal) }
                    .border(2.dp, borderBrush, RoundedCornerShape(14.dp))
                    .testTag("day_selector_$dayVal"),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = getDayAbbrev(dayVal),
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = if (isSelected) Color.White else Color.White.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${dayTotalHours}h",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Black),
                        color = if (isOverloaded) ColorOverload else Color.White
                    )
                    if (isOverloaded) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(ColorOverload)
                        )
                    }
                }
            }
        }
    }
}

// Beautiful list block representation
@Composable
fun TaskBlockCard(
    task: Task,
    onToggleComplete: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onMoveDayClick: (Int) -> Unit,
    onShiftOrderClick: (Boolean) -> Unit
) {
    val categoryColor = getCategoryColor(task.category)
    val priorityIcon = when (task.priority.lowercase().trim()) {
        "alta" -> "⚡"
        "media" -> "●"
        else -> "○"
    }
    val priorityColor = when (task.priority.lowercase().trim()) {
        "alta" -> ColorOverload
        "media" -> ColorOcio
        else -> ColorDeporte
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = SlateCardBg),
        shape = RoundedCornerShape(12.dp),
        border = BoxBorder(1.dp, BorderColor),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("task_card_${task.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left category color bar decoration
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(48.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(categoryColor)
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Checkbox completion
            IconButton(
                onClick = onToggleComplete,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = if (task.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                    contentDescription = "Completar tarea",
                    tint = if (task.isCompleted) ColorDeporte else Color.White.copy(alpha = 0.4f),
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Text detail
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Bold,
                            textDecoration = if (task.isCompleted) androidx.compose.ui.text.style.TextDecoration.LineThrough else null
                        ),
                        color = if (task.isCompleted) Color.White.copy(alpha = 0.5f) else Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    Icon(
                        imageVector = getCategoryIcon(task.category),
                        contentDescription = "Categoría",
                        tint = categoryColor,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = task.category.uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = categoryColor
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "${task.durationHours} hrs",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    // Priority tag
                    Text(
                        text = "$priorityIcon ${task.priority.uppercase()}",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = priorityColor
                    )
                }
            }

            // Quick reordering actions and Day shifting
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                // Move back a day
                if (task.dayOfWeek > 1) {
                    IconButton(
                        onClick = { onMoveDayClick(task.dayOfWeek - 1) },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(imageVector = Icons.Default.ChevronLeft, contentDescription = "Día Anterior", tint = Color.White.copy(alpha = 0.7f))
                    }
                }
                
                // Reorder vertically up
                IconButton(
                    onClick = { onShiftOrderClick(true) },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(imageVector = Icons.Default.ArrowUpward, contentDescription = "Subir", tint = Color.White.copy(alpha = 0.4f))
                }
                // Reorder vertically down
                IconButton(
                    onClick = { onShiftOrderClick(false) },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(imageVector = Icons.Default.ArrowDownward, contentDescription = "Bajar", tint = Color.White.copy(alpha = 0.4f))
                }

                // Move forward a day
                if (task.dayOfWeek < 7) {
                    IconButton(
                        onClick = { onMoveDayClick(task.dayOfWeek + 1) },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(imageVector = Icons.Default.ChevronRight, contentDescription = "Siguiente Día", tint = Color.White.copy(alpha = 0.7f))
                    }
                }

                // Edit
                IconButton(
                    onClick = onEditClick,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = "Editar", tint = ColorOcio)
                }

                // Delete
                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Eliminar", tint = ColorOverload)
                }
            }
        }
    }
}

// Dialog to add or edit blocks
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddEditTaskDialog(
    task: Task?,
    dayOfWeek: Int,
    onDismiss: () -> Unit,
    onSave: (String, Double, String, String, Int) -> Unit
) {
    var title by remember { mutableStateOf(task?.title ?: "") }
    var duration by remember { mutableStateOf(task?.durationHours ?: 1.0) }
    var category by remember { mutableStateOf(task?.category ?: "trabajo") }
    var priority by remember { mutableStateOf(task?.priority ?: "media") }
    var day by remember { mutableStateOf(task?.dayOfWeek ?: dayOfWeek) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onSave(title.trim(), duration, category, priority, day)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = ColorPrimaryNeon),
                enabled = title.isNotBlank()
            ) {
                Text(if (task == null) "Añadir Bloque" else "Guardar Cambios", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = Color.White.copy(alpha = 0.6f))
            }
        },
        title = {
            Text(
                text = if (task == null) "Nuevo Bloque de Tiempo" else "Editar Bloque de Tiempo",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.White
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(androidx.compose.foundation.rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Task Title
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Nombre del Bloque") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedLabelColor = ColorPrimaryNeon,
                        unfocusedLabelColor = Color.White.copy(alpha = 0.6f),
                        focusedBorderColor = ColorPrimaryNeon,
                        unfocusedBorderColor = BorderColor
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("add_task_title")
                )

                // Duration slider
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Duración", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.6f))
                        Text("${duration} hrs", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = ColorOcio)
                    }
                    Slider(
                        value = duration.toFloat(),
                        onValueChange = { duration = Math.round(it * 2) / 2.0 }, // Round to nearest 0.5
                        valueRange = 0.5f..8.0f,
                        steps = 14,
                        colors = SliderDefaults.colors(
                            thumbColor = ColorPrimaryNeon,
                            activeTrackColor = ColorPrimaryNeon,
                            inactiveTrackColor = BorderColor
                        ),
                        modifier = Modifier.testTag("add_task_duration_slider")
                    )
                }

                // Category row selector
                Column {
                    Text("Categoría", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.6f))
                    Spacer(modifier = Modifier.height(6.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val cats = listOf("trabajo", "estudio", "deporte", "ocio")
                        cats.forEach { cat ->
                            val isSelected = category.lowercase() == cat
                            val catColor = getCategoryColor(cat)
                            val brush = if (isSelected) Brush.linearGradient(listOf(catColor, catColor)) else Brush.linearGradient(listOf(Color.Transparent, Color.Transparent))
                            
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) catColor.copy(alpha = 0.15f) else SlateCardBg)
                                    .clickable { category = cat }
                                    .border(2.dp, if (isSelected) catColor else BorderColor, RoundedCornerShape(8.dp))
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = getCategoryIcon(cat),
                                        contentDescription = cat,
                                        tint = catColor,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = cat.uppercase(),
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = if (isSelected) Color.White else Color.White.copy(alpha = 0.6f)
                                    )
                                }
                            }
                        }
                    }
                }

                // Priority segmented selector
                Column {
                    Text("Prioridad", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.6f))
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val priorities = listOf("baja", "media", "alta")
                        priorities.forEach { prio ->
                            val isSelected = priority.lowercase() == prio
                            val pColor = when (prio) {
                                "alta" -> ColorOverload
                                "media" -> ColorOcio
                                else -> ColorDeporte
                            }
                            
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) pColor.copy(alpha = 0.15f) else SlateCardBg)
                                    .clickable { priority = prio }
                                    .border(2.dp, if (isSelected) pColor else BorderColor, RoundedCornerShape(8.dp))
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = prio.uppercase(),
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (isSelected) Color.White else Color.White.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }
                }

                // Day of Week selector
                Column {
                    Text("Día de la semana", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.6f))
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items((1..7).toList()) { d ->
                            val isSelected = day == d
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(if (isSelected) ColorPrimaryNeon else SlateCardBg)
                                    .clickable { day = d }
                                    .border(1.dp, if (isSelected) ColorPrimaryNeon else BorderColor, CircleShape)
                                    .size(36.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = getDayAbbrev(d),
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (isSelected) Color.White else Color.White.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }
                }
            }
        },
        containerColor = SlateCardBg,
        shape = RoundedCornerShape(16.dp)
    )
}

// Simple Empty state
@Composable
fun EmptyStateBlock(onQuickAddTask: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SlateCardBg),
        shape = RoundedCornerShape(12.dp),
        border = BoxBorder(1.dp, BorderColor),
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.EventNote,
                contentDescription = "No tasks",
                tint = Color.White.copy(alpha = 0.3f),
                modifier = Modifier.size(56.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Sin tareas asignadas",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.White
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Agrega bloques de tiempo hoy para optimizar tu jornada semanal.",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onQuickAddTask,
                colors = ButtonDefaults.buttonColors(containerColor = ColorPrimaryNeon)
            ) {
                Text("Crear un bloque rápido", fontWeight = FontWeight.Bold)
            }
        }
    }
}

// Helper to create Border object in Compose
@Composable
fun BoxBorder(width: androidx.compose.ui.unit.Dp, color: Color): androidx.compose.foundation.BorderStroke {
    return androidx.compose.foundation.BorderStroke(width, color)
}

// MODO ENFOQUE VIEW
@Composable
fun ModoEnfoqueView(tasks: List<Task>, onToggleComplete: (Task) -> Unit) {
    val calendar = Calendar.getInstance()
    val todayIdx = when (calendar.get(Calendar.DAY_OF_WEEK)) {
        Calendar.MONDAY -> 1
        Calendar.TUESDAY -> 2
        Calendar.WEDNESDAY -> 3
        Calendar.THURSDAY -> 4
        Calendar.FRIDAY -> 5
        Calendar.SATURDAY -> 6
        Calendar.SUNDAY -> 7
        else -> 1
    }

    val todayTasks = tasks.filter { it.dayOfWeek == todayIdx }.sortedBy { it.orderIndex }
    val remainingTasks = todayTasks.filter { !it.isCompleted }

    // Mock countdown timer for current focused task
    val currentFocusedTask = remainingTasks.firstOrNull()
    var ticksSeconds by remember { mutableStateOf(1680) } // 28 minutes left mock
    
    LaunchedEffect(currentFocusedTask) {
        if (currentFocusedTask != null) {
            ticksSeconds = (currentFocusedTask.durationHours * 3600 * 0.4).toInt() // starts roughly halfway through for nice animation demo
            while (ticksSeconds > 0) {
                delay(1000)
                ticksSeconds--
            }
        }
    }

    val displayMin = ticksSeconds / 60
    val displaySec = ticksSeconds % 60
    val timeLabel = String.format("%02d:%02d", displayMin, displaySec)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp, 8.dp, 16.dp, 80.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Main Focus Block Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SlateCardBg),
                border = BoxBorder(2.dp, if (currentFocusedTask != null) getCategoryColor(currentFocusedTask.category) else ColorPrimaryNeon),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("focused_block_hero")
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "ENFOQUE DE HOY",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.5.sp
                        ),
                        color = ColorPrimaryNeon
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    if (currentFocusedTask != null) {
                        val catColor = getCategoryColor(currentFocusedTask.category)
                        
                        Text(
                            text = currentFocusedTask.title,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = getCategoryIcon(currentFocusedTask.category),
                                contentDescription = null,
                                tint = catColor,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = currentFocusedTask.category.uppercase(),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = catColor
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                        
                        // Circular pulse design details
                        Box(
                            modifier = Modifier
                                .size(140.dp)
                                .clip(CircleShape)
                                .background(catColor.copy(alpha = 0.05f))
                                .border(4.dp, catColor, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = timeLabel,
                                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black),
                                color = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Completion trigger
                        Button(
                            onClick = { onToggleComplete(currentFocusedTask) },
                            colors = ButtonDefaults.buttonColors(containerColor = ColorDeporte),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().height(48.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Check, contentDescription = "Complete")
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Completar Bloque", fontWeight = FontWeight.Bold)
                            }
                        }
                    } else {
                        Icon(
                            imageVector = Icons.Default.DoneAll,
                            contentDescription = "Completado",
                            tint = ColorDeporte,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "¡Al día con tu agenda!",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                        Text(
                            text = "Has completado todos los bloques asignados para hoy. ¡Excelente trabajo!",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.6f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }
            }
        }

        // Checklist titles
        item {
            Text(
                text = "Cronograma de hoy (${getDayName(todayIdx)})",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.White,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        if (todayTasks.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SlateCardBg),
                    border = BoxBorder(1.dp, BorderColor),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                ) {
                    Text(
                        text = "No tienes bloques asignados para hoy. ¡Disfruta de un merecido descanso o agrega tareas en la pestaña Planificador Semanal!",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.6f),
                        modifier = Modifier.padding(16.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            items(todayTasks) { task ->
                val catColor = getCategoryColor(task.category)
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (task.isCompleted) SlateCardBg.copy(alpha = 0.6f) else SlateCardBg
                    ),
                    shape = RoundedCornerShape(12.dp),
                    border = BoxBorder(
                        width = 1.dp,
                        color = if (currentFocusedTask?.id == task.id) catColor else BorderColor
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Check trigger
                        IconButton(
                            onClick = { onToggleComplete(task) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = if (task.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                contentDescription = "Toggle Complete",
                                tint = if (task.isCompleted) ColorDeporte else Color.White.copy(alpha = 0.4f),
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = task.title,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    textDecoration = if (task.isCompleted) androidx.compose.ui.text.style.TextDecoration.LineThrough else null
                                ),
                                color = if (task.isCompleted) Color.White.copy(alpha = 0.5f) else Color.White
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(top = 2.dp)
                            ) {
                                Icon(
                                    imageVector = getCategoryIcon(task.category),
                                    contentDescription = null,
                                    tint = catColor,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = task.category.uppercase(),
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = catColor
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "${task.durationHours} hrs",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.5f)
                                )
                            }
                        }

                        if (currentFocusedTask?.id == task.id) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(catColor.copy(alpha = 0.15f))
                                    .padding(horizontal = 6.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "EN CURSO",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                                    color = catColor
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
