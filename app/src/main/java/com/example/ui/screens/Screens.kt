package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.FileProvider
import com.example.data.Attendance
import com.example.data.Group
import com.example.data.Payment
import com.example.data.Student
import com.example.ui.AlAtheerViewModel
import com.example.ui.PdfReportExporter
import com.example.ui.theme.StatusAbsent
import com.example.ui.theme.StatusLate
import com.example.ui.theme.StatusPaid
import com.example.ui.theme.StatusPresent
import com.example.ui.theme.StatusUnpaid
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

// --- HELPER FUNCTION: Launch WhatsApp ---
fun launchWhatsApp(context: Context, phone: String, message: String) {
    try {
        var cleanPhone = phone.trim()
        if (cleanPhone.startsWith("0")) {
            cleanPhone = "+2$cleanPhone"
        }
        val encodedMessage = Uri.encode(message)
        val url = "https://api.whatsapp.com/send?phone=$cleanPhone&text=$encodedMessage"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "لم يتم العثور على تطبيق واتساب أو الرابط غير صالح", Toast.LENGTH_SHORT).show()
    }
}

// ==========================================
// 1. MAIN DASHBOARD (HOME SCREEN)
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainDashboardScreen(
    viewModel: AlAtheerViewModel,
    onNavigate: (String) -> Unit
) {
    val studentsList by viewModel.students.collectAsState()
    val groupsList by viewModel.groups.collectAsState()

    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "الأثيـر للدروس الخصوصية",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 20.sp
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = primaryColor
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .background(
                            Brush.linearGradient(
                                colors = listOf(primaryColor, secondaryColor)
                            )
                        )
                        .padding(20.dp)
                        .fillMaxWidth()
                ) {
                    Column {
                        Text(
                            "أهلاً بك يا معلمنا الفاضل 👋",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "يمكنك الآن متابعة الطلاب والمجموعات والمدفوعات والحضور بكل سهولة ويسر.",
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            DashboardStatItem(label = "إجمالي الطلاب", value = "${studentsList.size}")
                            DashboardStatItem(label = "إجمالي المجموعات", value = "${groupsList.size}")
                        }
                    }
                }
            }

            Text(
                "لوحة التحكم السريعة",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            val menuItems = listOf(
                DashboardMenuItem("الطلاب", Icons.Rounded.Person, "students", StatusPresent),
                DashboardMenuItem("المجموعات", Icons.Rounded.Group, "groups", StatusLate),
                DashboardMenuItem("التحضير والحضور", Icons.Rounded.CheckCircle, "attendance", StatusPresent),
                DashboardMenuItem("الاشتراكات والمدفوعات", Icons.Rounded.Payments, "payments", StatusLate),
                DashboardMenuItem("التقارير والإحصائيات", Icons.Rounded.BarChart, "reports", MaterialTheme.colorScheme.primary),
                DashboardMenuItem("الإعدادات والنسخ", Icons.Rounded.Settings, "settings", Color.Gray)
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(menuItems) { item ->
                    DashboardGridCard(item = item, onClick = { onNavigate(item.route) })
                }
            }
        }
    }
}

data class DashboardMenuItem(
    val title: String,
    val icon: ImageVector,
    val route: String,
    val color: Color
)

@Composable
fun DashboardStatItem(label: String, value: String) {
    Column {
        Text(label, color = Color.White.copy(alpha = 0.75f), fontSize = 11.sp)
        Text(value, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun DashboardGridCard(item: DashboardMenuItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(115.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(item.color.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.title,
                    tint = item.color,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = item.title,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                lineHeight = 16.sp
            )
        }
    }
}

// ==========================================
// 2. GROUPS MANAGEMENT SCREEN
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupsScreen(
    viewModel: AlAtheerViewModel,
    onNavigateBack: () -> Unit,
    onEditGroup: (Int) -> Unit
) {
    val groupsList by viewModel.groups.collectAsState()
    val studentCounts by viewModel.groupStudentCounts.collectAsState()
    val searchQuery by viewModel.groupSearchQuery.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }

    val filteredGroups = groupsList.filter {
        it.name.contains(searchQuery, ignoreCase = true) || it.stage.contains(searchQuery, ignoreCase = true)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("إدارة المجموعات") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.Rounded.ArrowBack, contentDescription = "رجوع")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddDialog = true },
                icon = { Icon(Icons.Rounded.Add, contentDescription = "إضافة") },
                text = { Text("مجموعة جديدة") },
                shape = RoundedCornerShape(50.dp)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.groupSearchQuery.value = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                placeholder = { Text("بحث باسم المجموعة أو المرحلة...") },
                leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = "بحث") },
                shape = RoundedCornerShape(12.dp)
            )

            if (filteredGroups.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Rounded.Group,
                            contentDescription = "",
                            modifier = Modifier.size(64.dp),
                            tint = Color.LightGray
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("لا يوجد مجموعات حالياً", color = Color.Gray)
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredGroups) { group ->
                        val count = studentCounts[group.id] ?: 0
                        GroupItemCard(
                            group = group,
                            studentCount = count,
                            onEdit = { onEditGroup(group.id) },
                            onDelete = { viewModel.deleteGroup(group.id) }
                        )
                    }
                }
            }
        }

        if (showAddDialog) {
            GroupFormDialog(
                onDismiss = { showAddDialog = false },
                onSave = { name, stage, days, time, fees, notes ->
                    viewModel.addGroup(name, stage, days, time, fees, notes)
                    showAddDialog = false
                }
            )
        }
    }
}

@Composable
fun GroupItemCard(
    group: Group,
    studentCount: Int,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        group.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        "المرحلة: ${group.stage}",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
                Row {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Rounded.Edit, contentDescription = "تعديل", tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = { showDeleteConfirm = true }) {
                        Icon(Icons.Rounded.Delete, contentDescription = "حذف", tint = Color.Red)
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), thickness = 0.5.dp)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("المواعيد والأيام", fontSize = 10.sp, color = Color.Gray)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.CalendarToday, contentDescription = "", modifier = Modifier.size(12.dp), tint = Color.Gray)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("${group.days} - ${group.time}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("الطلاب والاشتراك", fontSize = 10.sp, color = Color.Gray)
                    Text(
                        "${studentCount} طالب • ${group.fees} ج.م",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("حذف المجموعة") },
            text = { Text("هل أنت متأكد من رغبتك في حذف مجموعة '${group.name}'؟ سيؤدي ذلك أيضاً لإلغاء ارتباط الطلاب بها.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteConfirm = false
                    }
                ) {
                    Text("نعم، حذف", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("إلغاء")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupFormDialog(
    group: Group? = null,
    onDismiss: () -> Unit,
    onSave: (name: String, stage: String, days: String, time: String, fees: Double, notes: String) -> Unit
) {
    var name by remember { mutableStateOf(group?.name ?: "") }
    var stage by remember { mutableStateOf(group?.stage ?: "") }
    var days by remember { mutableStateOf(group?.days ?: "") }
    var time by remember { mutableStateOf(group?.time ?: "") }
    var feesStr by remember { mutableStateOf(group?.fees?.toString() ?: "") }
    var notes by remember { mutableStateOf(group?.notes ?: "") }

    val stages = listOf("ابتدائي", "متوسط", "ثانوي", "جامعي")
    var expandedStage by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            LazyColumn(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Text(
                        if (group == null) "إضافة مجموعة جديدة" else "تعديل المجموعة",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                item {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("اسم المجموعة") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = stage,
                            onValueChange = {},
                            label = { Text("المرحلة الدراسية") },
                            readOnly = true,
                            modifier = Modifier.fillMaxWidth(),
                            trailingIcon = {
                                IconButton(onClick = { expandedStage = !expandedStage }) {
                                    Icon(Icons.Rounded.ArrowDropDown, contentDescription = "")
                                }
                            }
                        )
                        DropdownMenu(
                            expanded = expandedStage,
                            onDismissRequest = { expandedStage = false }
                        ) {
                            stages.forEach { item ->
                                DropdownMenuItem(
                                    text = { Text(item) },
                                    onClick = {
                                        stage = item
                                        expandedStage = false
                                    }
                                )
                            }
                        }
                    }
                }
                item {
                    OutlinedTextField(
                        value = days,
                        onValueChange = { days = it },
                        label = { Text("أيام الدراسة (مثال: السبت والثلاثاء)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = time,
                        onValueChange = { time = it },
                        label = { Text("توقيت الدرس (مثال: 04:00 مساءً)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = feesStr,
                        onValueChange = { feesStr = it },
                        label = { Text("قيمة الاشتراك الشهري (ج.م)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("ملاحظات إضافية") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = onDismiss) {
                            Text("إلغاء")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val fees = feesStr.toDoubleOrNull() ?: 0.0
                                if (name.isNotBlank() && stage.isNotBlank()) {
                                    onSave(name, stage, days, time, fees, notes)
                                }
                            },
                            shape = RoundedCornerShape(50.dp)
                        ) {
                            Text("حفظ")
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 3. EDIT GROUP SCREEN
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditGroupScreen(
    groupId: Int,
    viewModel: AlAtheerViewModel,
    onNavigateBack: () -> Unit
) {
    val groupsList by viewModel.groups.collectAsState()
    val group = groupsList.find { it.id == groupId }

    if (group != null) {
        var name by remember { mutableStateOf(group.name) }
        var stage by remember { mutableStateOf(group.stage) }
        var days by remember { mutableStateOf(group.days) }
        var time by remember { mutableStateOf(group.time) }
        var feesStr by remember { mutableStateOf(group.fees.toString()) }
        var notes by remember { mutableStateOf(group.notes) }

        val stages = listOf("ابتدائي", "متوسط", "ثانوي", "جامعي")
        var expandedStage by remember { mutableStateOf(false) }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("تعديل تفاصيل المجموعة") },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(imageVector = Icons.Rounded.ArrowBack, contentDescription = "رجوع")
                        }
                    }
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("اسم المجموعة") },
                    modifier = Modifier.fillMaxWidth()
                )

                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = stage,
                        onValueChange = {},
                        label = { Text("المرحلة الدراسية") },
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            IconButton(onClick = { expandedStage = !expandedStage }) {
                                Icon(Icons.Rounded.ArrowDropDown, contentDescription = "")
                            }
                        }
                    )
                    DropdownMenu(
                        expanded = expandedStage,
                        onDismissRequest = { expandedStage = false }
                    ) {
                        stages.forEach { item ->
                            DropdownMenuItem(
                                text = { Text(item) },
                                onClick = {
                                    stage = item
                                    expandedStage = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = days,
                    onValueChange = { days = it },
                    label = { Text("الأيام") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = time,
                    onValueChange = { time = it },
                    label = { Text("التوقيت") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = feesStr,
                    onValueChange = { feesStr = it },
                    label = { Text("الاشتراك الشهري") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("الملاحظات") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = {
                        val fees = feesStr.toDoubleOrNull() ?: 0.0
                        viewModel.updateGroup(
                            group.copy(
                                name = name,
                                stage = stage,
                                days = days,
                                time = time,
                                fees = fees,
                                notes = notes
                            )
                        )
                        onNavigateBack()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(50.dp)
                ) {
                    Text("حفظ التغييرات", fontSize = 16.sp)
                }
            }
        }
    }
}

// ==========================================
// 4. STUDENTS LIST SCREEN
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentsScreen(
    viewModel: AlAtheerViewModel,
    onNavigateBack: () -> Unit,
    onViewProfile: (Int) -> Unit
) {
    val studentsList by viewModel.students.collectAsState()
    val groupsList by viewModel.groups.collectAsState()
    val searchQuery by viewModel.studentSearchQuery.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }

    val filteredStudents = studentsList.filter { student ->
        val groupName = groupsList.find { it.id == student.groupId }?.name ?: ""
        student.name.contains(searchQuery, ignoreCase = true) ||
                student.parentPhone.contains(searchQuery) ||
                groupName.contains(searchQuery, ignoreCase = true)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("إدارة الطلاب") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.Rounded.ArrowBack, contentDescription = "رجوع")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddDialog = true },
                icon = { Icon(Icons.Rounded.Add, contentDescription = "إضافة") },
                text = { Text("طالب جديد") },
                shape = RoundedCornerShape(50.dp)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.studentSearchQuery.value = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                placeholder = { Text("بحث باسم الطالب أو الهاتف أو المجموعة...") },
                leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = "بحث") },
                shape = RoundedCornerShape(12.dp)
            )

            if (filteredStudents.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Rounded.Person,
                            contentDescription = "",
                            modifier = Modifier.size(64.dp),
                            tint = Color.LightGray
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("لا يوجد طلاب حالياً", color = Color.Gray)
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredStudents) { student ->
                        val group = groupsList.find { it.id == student.groupId }
                        StudentItemCard(
                            student = student,
                            groupName = group?.name ?: "غير محدد",
                            onClick = { onViewProfile(student.id) },
                            onDelete = { viewModel.deleteStudent(student.id) }
                        )
                    }
                }
            }
        }

        if (showAddDialog) {
            StudentFormDialog(
                groups = groupsList,
                onDismiss = { showAddDialog = false },
                onSave = { name, parentPhone, groupId, notes ->
                    val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                    viewModel.addStudent(name, parentPhone, groupId, dateStr, notes)
                    showAddDialog = false
                }
            )
        }
    }
}

@Composable
fun StudentItemCard(
    student: Student,
    groupName: String,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Person,
                        contentDescription = "",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        student.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        "المجموعة: $groupName",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        "ولي الأمر: ${student.parentPhone}",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }
            }
            IconButton(onClick = { showDeleteConfirm = true }) {
                Icon(Icons.Rounded.Delete, contentDescription = "حذف", tint = Color.Red)
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("حذف الطالب") },
            text = { Text("هل أنت متأكد من رغبتك في حذف الطالب '${student.name}'؟ سيؤدي ذلك لحذف سجله تماماً.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteConfirm = false
                    }
                ) {
                    Text("نعم، حذف", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("إلغاء")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentFormDialog(
    student: Student? = null,
    groups: List<Group>,
    onDismiss: () -> Unit,
    onSave: (name: String, parentPhone: String, groupId: Int, notes: String) -> Unit
) {
    var name by remember { mutableStateOf(student?.name ?: "") }
    var parentPhone by remember { mutableStateOf(student?.parentPhone ?: "") }
    var groupId by remember { mutableStateOf(student?.groupId ?: (groups.firstOrNull()?.id ?: 0)) }
    var notes by remember { mutableStateOf(student?.notes ?: "") }

    var expandedGroup by remember { mutableStateOf(false) }
    val selectedGroup = groups.find { it.id == groupId }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            LazyColumn(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Text(
                        if (student == null) "تسجيل طالب جديد" else "تعديل بيانات الطالب",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                item {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("اسم الطالب ثلاثي") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = parentPhone,
                        onValueChange = { parentPhone = it },
                        label = { Text("رقم هاتف ولي الأمر (واتساب)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = selectedGroup?.name ?: "اختر مجموعة",
                            onValueChange = {},
                            label = { Text("المجموعة") },
                            readOnly = true,
                            modifier = Modifier.fillMaxWidth(),
                            trailingIcon = {
                                IconButton(onClick = { expandedGroup = !expandedGroup }) {
                                    Icon(Icons.Rounded.ArrowDropDown, contentDescription = "")
                                }
                            }
                        )
                        DropdownMenu(
                            expanded = expandedGroup,
                            onDismissRequest = { expandedGroup = false }
                        ) {
                            groups.forEach { group ->
                                DropdownMenuItem(
                                    text = { Text(group.name) },
                                    onClick = {
                                        groupId = group.id
                                        expandedGroup = false
                                    }
                                )
                            }
                        }
                    }
                }
                item {
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("ملاحظات") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = onDismiss) {
                            Text("إلغاء")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (name.isNotBlank() && parentPhone.isNotBlank() && groupId > 0) {
                                    onSave(name, parentPhone, groupId, notes)
                                }
                            },
                            shape = RoundedCornerShape(50.dp)
                        ) {
                            Text("حفظ")
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 5. STUDENT PROFILE SCREEN
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentProfileScreen(
    studentId: Int,
    viewModel: AlAtheerViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current

    val studentsList by viewModel.students.collectAsState()
    val groupsList by viewModel.groups.collectAsState()
    val attendanceList by viewModel.attendance.collectAsState()
    val paymentsList by viewModel.payments.collectAsState()

    val student = studentsList.find { it.id == studentId }
    val group = groupsList.find { it.id == student?.groupId }

    val studentAttendance = attendanceList.filter { it.studentId == studentId }
    val studentPayments = paymentsList.filter { it.studentId == studentId }

    val presentCount = studentAttendance.count { it.status == "PRESENT" }
    val lateCount = studentAttendance.count { it.status == "LATE" }
    val absentCount = studentAttendance.count { it.status == "ABSENT" }
    val totalLessons = studentAttendance.size

    val attendancePercentage = if (totalLessons > 0) {
        ((presentCount + lateCount) * 100 / totalLessons)
    } else {
        100
    }

    var showEditDialog by remember { mutableStateOf(false) }
    var showWhatsAppDialog by remember { mutableStateOf(false) }

    if (student != null) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("ملف الطالب") },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(imageVector = Icons.Rounded.ArrowBack, contentDescription = "رجوع")
                        }
                    },
                    actions = {
                        IconButton(onClick = { showEditDialog = true }) {
                            Icon(Icons.Rounded.Edit, contentDescription = "تعديل")
                        }
                    }
                )
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(60.dp)
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        student.name.take(1),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 24.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text(
                                        student.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        "المجموعة: ${group?.name ?: "غير محدد"}",
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                }
                            }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), thickness = 0.5.dp)

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                ProfileInfoColumn(label = "هاتف ولي الأمر", value = student.parentPhone)
                                ProfileInfoColumn(label = "تاريخ الانضمام", value = student.joinDate)
                            }

                            if (student.notes.isNotBlank()) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Text("ملاحظات:", fontSize = 11.sp, color = Color.Gray)
                                Text(student.notes, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Card(
                            modifier = Modifier
                                .weight(1.2f)
                                .height(110.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text("نسبة الحضور", fontSize = 11.sp, color = Color.Gray)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    "$attendancePercentage%",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 24.sp,
                                    color = if (attendancePercentage >= 85) StatusPresent else if (attendancePercentage >= 60) StatusLate else Color.Red
                                )
                                Text("إجمالي الدروس: $totalLessons", fontSize = 10.sp, color = Color.Gray)
                            }
                        }

                        Card(
                            modifier = Modifier
                                .weight(1.8f)
                                .height(110.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                StatCounter(label = "حاضر", count = presentCount, color = StatusPresent)
                                StatCounter(label = "متأخر", count = lateCount, color = StatusLate)
                                StatCounter(label = "غائب", count = absentCount, color = Color.Red)
                            }
                        }
                    }
                }

                item {
                    Button(
                        onClick = { showWhatsAppDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Rounded.Phone, contentDescription = "", tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("مراسلة ولي الأمر عبر واتساب", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "سجل الاشتراكات الشهرية",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            if (studentPayments.isEmpty()) {
                                Text("لا يوجد سجل مدفوعات مسجل حالياً.", fontSize = 12.sp, color = Color.Gray)
                            } else {
                                studentPayments.forEach { payment ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(payment.month, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                        Box(
                                            modifier = Modifier
                                                .background(
                                                    if (payment.status == "PAID") StatusPresent.copy(alpha = 0.15f) else Color.Red.copy(alpha = 0.15f),
                                                    RoundedCornerShape(50.dp)
                                                )
                                                .padding(horizontal = 10.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = if (payment.status == "PAID") "تم الدفع" else "لم يدفع",
                                                color = if (payment.status == "PAID") StatusPresent else Color.Red,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "سجل حضور المحاضرات",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            if (studentAttendance.isEmpty()) {
                                Text("لا يوجد سجل حضور مسجل حالياً.", fontSize = 12.sp, color = Color.Gray)
                            } else {
                                studentAttendance.forEach { record ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(record.date, fontSize = 13.sp)
                                        Box(
                                            modifier = Modifier
                                                .background(
                                                    when (record.status) {
                                                        "PRESENT" -> StatusPresent.copy(alpha = 0.15f)
                                                        "LATE" -> StatusLate.copy(alpha = 0.15f)
                                                        else -> Color.Red.copy(alpha = 0.15f)
                                                    },
                                                    RoundedCornerShape(50.dp)
                                                )
                                                .padding(horizontal = 10.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = when (record.status) {
                                                    "PRESENT" -> "حاضر"
                                                    "LATE" -> "متأخر"
                                                    else -> "غائب"
                                                },
                                                color = when (record.status) {
                                                    "PRESENT" -> StatusPresent
                                                    "LATE" -> StatusLate
                                                    else -> Color.Red
                                                },
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showEditDialog) {
            StudentFormDialog(
                student = student,
                groups = groupsList,
                onDismiss = { showEditDialog = false },
                onSave = { name, parentPhone, groupId, notes ->
                    viewModel.updateStudent(student.copy(name = name, parentPhone = parentPhone, groupId = groupId, notes = notes))
                    showEditDialog = false
                }
            )
        }

        if (showWhatsAppDialog) {
            WhatsAppTemplatesDialog(
                studentName = student.name,
                onDismiss = { showWhatsAppDialog = false },
                onSend = { message ->
                    launchWhatsApp(context, student.parentPhone, message)
                    showWhatsAppDialog = false
                }
            )
        }
    }
}

// ==========================================
// 6. ATTENDANCE MARKING SCREEN
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceScreen(
    viewModel: AlAtheerViewModel,
    onNavigateBack: () -> Unit
) {
    val groupsList by viewModel.groups.collectAsState()

    var selectedGroupId by remember { mutableStateOf(groupsList.firstOrNull()?.id ?: 0) }
    val currentDate = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()) }
    var selectedDate by remember { mutableStateOf(currentDate) }

    val studentsWithAttendance by viewModel.getStudentsWithAttendance(selectedGroupId, selectedDate).collectAsState()

    val presentCount = studentsWithAttendance.count { it.second == "PRESENT" }
    val lateCount = studentsWithAttendance.count { it.second == "LATE" }
    val absentCount = studentsWithAttendance.count { it.second == "ABSENT" }

    var expandedGroup by remember { mutableStateOf(false) }
    val selectedGroup = groupsList.find { it.id == selectedGroupId } ?: groupsList.firstOrNull()

    LaunchedEffect(groupsList) {
        if (selectedGroupId == 0 && groupsList.isNotEmpty()) {
            selectedGroupId = groupsList.first().id
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("تحضير الحضور والغياب") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.Rounded.ArrowBack, contentDescription = "رجوع")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    OutlinedTextField(
                        value = selectedGroup?.name ?: "اختر مجموعة",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("المجموعة") },
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            IconButton(onClick = { expandedGroup = !expandedGroup }) {
                                Icon(Icons.Rounded.ArrowDropDown, contentDescription = "")
                            }
                        }
                    )
                    DropdownMenu(
                        expanded = expandedGroup,
                        onDismissRequest = { expandedGroup = false }
                    ) {
                        groupsList.forEach { group ->
                            DropdownMenuItem(
                                text = { Text(group.name) },
                                onClick = {
                                    selectedGroupId = group.id
                                    expandedGroup = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = selectedDate,
                    onValueChange = { selectedDate = it },
                    label = { Text("تاريخ المحاضرة") },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("YYYY-MM-DD") }
                )
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    AttendanceSummaryItem(label = "حاضر", count = presentCount, color = StatusPresent)
                    AttendanceSummaryItem(label = "متأخر", count = lateCount, color = StatusLate)
                    AttendanceSummaryItem(label = "غائب", count = absentCount, color = Color.Red)
                }
            }

            if (studentsWithAttendance.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("لا يوجد طلاب مسجلين في هذه المجموعة.", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(studentsWithAttendance) { (student, status) ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    student.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    modifier = Modifier.weight(1.2f)
                                )

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.weight(1.8f)
                                ) {
                                    AttendanceStateButton(
                                        label = "حاضر",
                                        isSelected = status == "PRESENT",
                                        color = StatusPresent,
                                        onClick = {
                                            viewModel.updateStudentAttendance(student.id, selectedGroupId, selectedDate, "PRESENT")
                                        }
                                    )
                                    AttendanceStateButton(
                                        label = "متأخر",
                                        isSelected = status == "LATE",
                                        color = StatusLate,
                                        onClick = {
                                            viewModel.updateStudentAttendance(student.id, selectedGroupId, selectedDate, "LATE")
                                        }
                                    )
                                    AttendanceStateButton(
                                        label = "غائب",
                                        isSelected = status == "ABSENT",
                                        color = Color.Red,
                                        onClick = {
                                            viewModel.updateStudentAttendance(student.id, selectedGroupId, selectedDate, "ABSENT")
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 7. PAYMENTS TRACKING SCREEN
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentsScreen(
    viewModel: AlAtheerViewModel,
    onNavigateBack: () -> Unit
) {
    val groupsList by viewModel.groups.collectAsState()

    var selectedGroupId by remember { mutableStateOf(groupsList.firstOrNull()?.id ?: 0) }

    val currentMonth = remember { SimpleDateFormat("yyyy-MM", Locale.US).format(Date()) }
    var selectedMonth by remember { mutableStateOf(currentMonth) }

    val studentsWithPayment by viewModel.getStudentsWithPayment(selectedGroupId, selectedMonth).collectAsState()
    val group = groupsList.find { it.id == selectedGroupId } ?: groupsList.firstOrNull()

    var expandedGroup by remember { mutableStateOf(false) }

    val paidCount = studentsWithPayment.count { it.second?.status == "PAID" }
    val unpaidCount = studentsWithPayment.size - paidCount
    val fees = group?.fees ?: 0.0
    val collectedAmount = paidCount * fees
    val remainingAmount = unpaidCount * fees

    LaunchedEffect(groupsList) {
        if (selectedGroupId == 0 && groupsList.isNotEmpty()) {
            selectedGroupId = groupsList.first().id
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("تحصيل الاشتراكات والمدفوعات") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.Rounded.ArrowBack, contentDescription = "رجوع")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    OutlinedTextField(
                        value = group?.name ?: "اختر مجموعة",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("المجموعة") },
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            IconButton(onClick = { expandedGroup = !expandedGroup }) {
                                Icon(Icons.Rounded.ArrowDropDown, contentDescription = "")
                            }
                        }
                    )
                    DropdownMenu(
                        expanded = expandedGroup,
                        onDismissRequest = { expandedGroup = false }
                    ) {
                        groupsList.forEach { grp ->
                            DropdownMenuItem(
                                text = { Text(grp.name) },
                                onClick = {
                                    selectedGroupId = grp.id
                                    expandedGroup = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = selectedMonth,
                    onValueChange = { selectedMonth = it },
                    label = { Text("الشهر الدراسي") },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("YYYY-MM") }
                )
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        PaymentSummaryBlock(label = "المبالغ المحصّلة", value = "$collectedAmount ج.م", color = StatusPresent)
                        PaymentSummaryBlock(label = "المبالغ المتبقية", value = "$remainingAmount ج.م", color = Color.Red)
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), thickness = 0.5.dp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("مسدد: $paidCount طالب", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = StatusPresent)
                        Text("غير مسدد: $unpaidCount طالب", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Red)
                    }
                }
            }

            if (studentsWithPayment.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("لا يوجد طلاب مسجلين في هذه المجموعة.", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(studentsWithPayment) { (student, payment) ->
                        val isPaid = payment?.status == "PAID"
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        student.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        "قيمة الاشتراك: $fees ج.م",
                                        fontSize = 11.sp,
                                        color = Color.Gray
                                    )
                                }

                                Button(
                                    onClick = {
                                        viewModel.toggleStudentPayment(student.id, selectedGroupId, selectedMonth, payment, fees)
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isPaid) StatusPresent else Color.Red.copy(alpha = 0.08f),
                                        contentColor = if (isPaid) Color.White else Color.Red
                                    ),
                                    shape = RoundedCornerShape(50.dp),
                                    border = BorderStroke(1.dp, if (isPaid) StatusPresent else Color.Red)
                                ) {
                                    if (isPaid) {
                                        Icon(Icons.Rounded.Check, contentDescription = "", modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("مسدد", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    } else {
                                        Icon(Icons.Rounded.Close, contentDescription = "", modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("غير مسدد", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 8. REPORTS SCREEN
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    viewModel: AlAtheerViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current

    val studentsList by viewModel.students.collectAsState()
    val groupsList by viewModel.groups.collectAsState()
    val attendanceList by viewModel.attendance.collectAsState()
    val paymentsList by viewModel.payments.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("تقارير وإحصائيات النظام") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.Rounded.ArrowBack, contentDescription = "رجوع")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "إحصائيات الأثير العامة",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        ReportStatBox(label = "إجمالي الطلاب", count = "${studentsList.size}", color = MaterialTheme.colorScheme.primary)
                        ReportStatBox(label = "إجمالي المجموعات", count = "${groupsList.size}", color = MaterialTheme.colorScheme.secondary)
                        ReportStatBox(
                            label = "نسبة الحضور الكلية",
                            count = if (attendanceList.isNotEmpty()) "${(attendanceList.count { it.status == "PRESENT" || it.status == "LATE" } * 100 / attendanceList.size)}%" else "100%",
                            color = StatusPresent
                        )
                    }
                }
            }

            Text("تصدير التقارير بصيغة PDF", fontWeight = FontWeight.Bold, fontSize = 14.sp)

            ReportActionCard(
                title = "تقرير الحضور والغياب للطلاب",
                description = "قائمة بجميع المحاضرات ومعدل حضور الطلاب ونسبة التزامهم بالمواعيد.",
                icon = Icons.Rounded.CheckCircle,
                color = StatusPresent,
                onClick = {
                    val headers = listOf("اسم الطالب", "حضور", "تأخر", "غياب", "نسبة الحضور")
                    val rows = studentsList.map { student ->
                        val sat = attendanceList.filter { it.studentId == student.id }
                        val pres = sat.count { it.status == "PRESENT" }
                        val lat = sat.count { it.status == "LATE" }
                        val abs = sat.count { it.status == "ABSENT" }
                        val percent = if (sat.isNotEmpty()) "${(pres + lat) * 100 / sat.size}%" else "100%"
                        listOf(student.name, "$pres", "$lat", "$abs", percent)
                    }
                    PdfReportExporter.exportToPdf(context, "تقرير الحضور والغياب المفصل للطلاب", headers, rows)
                }
            )

            ReportActionCard(
                title = "تقرير الاشتراكات والمدفوعات المالية",
                description = "إحصائيات الاشتراكات المحصلة والمتبقية وأسماء الطلاب المتخلفين عن السداد.",
                icon = Icons.Rounded.Payments,
                color = StatusLate,
                onClick = {
                    val headers = listOf("اسم الطالب", "الشهر الدراسي", "الحالة", "الاشتراك الشهري")
                    val rows = paymentsList.map { payment ->
                        val s = studentsList.find { it.id == payment.studentId }?.name ?: "غير معروف"
                        val status = if (payment.status == "PAID") "تم الدفع" else "لم يدفع"
                        listOf(s, payment.month, status, "${payment.amountPaid} ج.م")
                    }
                    PdfReportExporter.exportToPdf(context, "تقرير المدفوعات والاشتراكات التفصيلي", headers, rows)
                }
            )

            ReportActionCard(
                title = "الطلاب الأكثر غياباً",
                description = "قائمة بالطلاب الذين يتجاوز غيابهم الحدود المسموح بها للمتابعة مع أولياء الأمور.",
                icon = Icons.Rounded.Warning,
                color = Color.Red,
                onClick = {
                    val headers = listOf("اسم الطالب", "إجمالي محاضرات الغياب", "هاتف ولي الأمر")
                    val rows = studentsList.map { student ->
                        val abs = attendanceList.count { it.studentId == student.id && it.status == "ABSENT" }
                        student to abs
                    }.sortedByDescending { it.second }.filter { it.second > 0 }.map { (st, abs) ->
                        listOf(st.name, "$abs محاضرة", st.parentPhone)
                    }
                    PdfReportExporter.exportToPdf(context, "تقرير الطلاب الأكثر غياباً والمتابعة", headers, rows)
                }
            )
        }
    }
}

// ==========================================
// 9. SETTINGS & LOCAL BACKUP SCREEN
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: AlAtheerViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val isDark by viewModel.isDarkMode.collectAsState()
    val backupsList by viewModel.backups.collectAsState()

    var showResetConfirm by remember { mutableStateOf(false) }

    val fileRestoreLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.restoreDatabaseFromUri(uri) { success ->
                if (success) {
                    Toast.makeText(context, "تم استعادة قاعدة البيانات بنجاح!", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(context, "فشل استعادة قاعدة البيانات، يرجى التحقق من صحة الملف.", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("إعدادات النظام والنسخ الاحتياطي") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.Rounded.ArrowBack, contentDescription = "رجوع")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "المظهر العام",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (isDark) Icons.Rounded.DarkMode else Icons.Rounded.LightMode,
                                    contentDescription = "",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("المظهر الداكن (Dark Mode)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text("تغيير واجهة التطبيق للوضع الليلي المريح للعين", fontSize = 11.sp, color = Color.Gray)
                                }
                            }
                            Switch(
                                checked = isDark,
                                onCheckedChange = { viewModel.setDarkMode(it) }
                            )
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            "النسخ الاحتياطي والاستعادة",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )

                        Button(
                            onClick = {
                                viewModel.backupDatabase { file ->
                                    if (file != null) {
                                        try {
                                            val fileUri = FileProvider.getUriForFile(
                                                context,
                                                "${context.packageName}.fileprovider",
                                                file
                                            )
                                            val intent = Intent(Intent.ACTION_SEND).apply {
                                                type = "application/octet-stream"
                                                putExtra(Intent.EXTRA_STREAM, fileUri)
                                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                            }
                                            context.startActivity(Intent.createChooser(intent, "تصدير وحفظ ملف النسخة الاحتياطية:"))
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "تم حفظ النسخة محلياً: ${file.name}", Toast.LENGTH_LONG).show()
                                        }
                                    } else {
                                        Toast.makeText(context, "فشل إنشاء نسخة احتياطية", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Rounded.CloudUpload, contentDescription = "")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("إنشاء نسخة احتياطية جديدة وتصديرها", fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                fileRestoreLauncher.launch("*/*")
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                        ) {
                            Icon(Icons.Rounded.CloudDownload, contentDescription = "")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("استعادة قاعدة بيانات من ملف خارجي", fontWeight = FontWeight.Bold)
                        }

                        if (backupsList.isNotEmpty()) {
                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), thickness = 0.5.dp)
                            Text("النسخ الاحتياطية السابقة محلياً:", fontSize = 11.sp, color = Color.Gray)
                            backupsList.take(3).forEach { b ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            viewModel.restoreDatabase(b.filePath) { success ->
                                                if (success) {
                                                    Toast.makeText(context, "تمت الاستعادة بنجاح!", Toast.LENGTH_SHORT).show()
                                                } else {
                                                    Toast.makeText(context, "فشلت الاستعادة.", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        }
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(File(b.filePath).name, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                        Text("التاريخ: ${b.date}", fontSize = 10.sp, color = Color.Gray)
                                    }
                                    Text("اضغط للاستعادة", fontSize = 10.sp, color = MaterialTheme.colorScheme.secondary)
                                }
                            }
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "منطقة الخطر",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color.Red,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        Button(
                            onClick = { showResetConfirm = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Rounded.Delete, contentDescription = "")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("تهيئة ومسح قاعدة البيانات تماماً", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "الأثير Al-Atheer",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "الإصدار 1.0.0 • تم التطوير كحل ذكي وشامل لإدارة الدروس والمجموعات والطلاب بكفاءة وسرعة فائقة.",
                            fontSize = 11.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }
    }

    if (showResetConfirm) {
        AlertDialog(
            onDismissRequest = { showResetConfirm = false },
            title = { Text("تأكيد تهيئة قاعدة البيانات") },
            text = { Text("هل أنت متأكد تماماً من رغبتك في حذف كافة المجموعات، الطلاب وسجلات الحضور والمدفوعات من التطبيق؟ لا يمكن التراجع عن هذا الإجراء!") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.resetDatabase {
                            Toast.makeText(context, "تم مسح البيانات بنجاح!", Toast.LENGTH_SHORT).show()
                        }
                        showResetConfirm = false
                    }
                ) {
                    Text("نعم، امسح كل شيء", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirm = false }) {
                    Text("إلغاء")
                }
            }
        )
    }
}

// ==========================================
// SUBCOMPOSABLES & HELPER CARDS
// ==========================================
@Composable
fun ProfileInfoColumn(label: String, value: String) {
    Column {
        Text(label, fontSize = 10.sp, color = Color.Gray)
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun StatCounter(label: String, count: Int, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(color.copy(alpha = 0.15f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text("$count", fontWeight = FontWeight.Bold, color = color, fontSize = 14.sp)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(label, fontSize = 11.sp, color = Color.Gray)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WhatsAppTemplatesDialog(
    studentName: String,
    onDismiss: () -> Unit,
    onSend: (String) -> Unit
) {
    val templates = listOf(
        "غياب الطالب" to "السلام عليكم ورحمة الله وبركاته، نحيطكم علماً بغياب الطالب ($studentName) عن محاضرة الدرس اليوم. نرجو منكم المتابعة والاهتمام.",
        "تأخر الطالب" to "السلام عليكم ورحمة الله وبركاته، نحيطكم علماً بتأخر الطالب ($studentName) عن موعد المحاضرة اليوم.",
        "تذكير بالاشتراك الشهري" to "السلام عليكم ورحمة الله وبركاته، نذكركم بلطف بضرورة سداد الاشتراك الشهري المقرر للطالب ($studentName). شاكرين لكم حسن تعاونكم.",
        "متابعة مستوى دراسي" to "السلام عليكم ورحمة الله وبركاته، نود إفادتكم بأن مستوى الطالب ($studentName) يحتاج إلى مزيد من المتابعة والاهتمام ومراجعة الدروس بالمنزل بشكل مستمر لرفع مستواه.",
        "يرجى التواصل" to "السلام عليكم ورحمة الله وبركاته، يرجى من ولي أمر الطالب ($studentName) التواصل مع المعلم لأمر هام يخص الطالب."
    )

    var selectedIndex by remember { mutableStateOf(0) }
    var editedMessage by remember { mutableStateOf(templates[0].second) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    "رسائل واتساب الجاهزة",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.primary
                )

                var expanded by remember { mutableStateOf(false) }
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = templates[selectedIndex].first,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("اختر النموذج") },
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            IconButton(onClick = { expanded = !expanded }) {
                                Icon(Icons.Rounded.ArrowDropDown, contentDescription = "")
                            }
                        }
                    )
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        templates.forEachIndexed { index, pair ->
                            DropdownMenuItem(
                                text = { Text(pair.first) },
                                onClick = {
                                    selectedIndex = index
                                    editedMessage = pair.second
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = editedMessage,
                    onValueChange = { editedMessage = it },
                    label = { Text("نص الرسالة (يمكنك تعديلها)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    maxLines = 6
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("إلغاء")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onSend(editedMessage) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                        shape = RoundedCornerShape(50.dp)
                    ) {
                        Text("إرسال عبر واتساب", color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun AttendanceSummaryItem(label: String, count: Int, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("$count", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = color)
        Text(label, fontSize = 11.sp, color = Color.Gray)
    }
}

@Composable
fun RowScope.AttendanceStateButton(
    label: String,
    isSelected: Boolean,
    color: Color,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) color else color.copy(alpha = 0.08f),
            contentColor = if (isSelected) Color.White else color
        ),
        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .weight(1f)
            .height(34.dp),
        border = BorderStroke(1.dp, color)
    ) {
        Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun PaymentSummaryBlock(label: String, value: String, color: Color) {
    Column {
        Text(label, fontSize = 11.sp, color = Color.Gray)
        Text(value, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = color)
    }
}

@Composable
fun ReportStatBox(label: String, count: String, color: Color) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.08f)),
        modifier = Modifier.width(95.dp)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(count, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = color)
            Spacer(modifier = Modifier.height(2.dp))
            Text(label, fontSize = 9.sp, color = Color.Gray, textAlign = TextAlign.Center)
        }
    }
}

@Composable
fun ReportActionCard(
    title: String,
    description: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .background(color.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = "", tint = color, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(description, fontSize = 11.sp, color = Color.Gray, lineHeight = 15.sp)
            }
            Icon(Icons.Rounded.ArrowForward, contentDescription = "", tint = Color.Gray, modifier = Modifier.size(16.dp))
        }
    }
}
