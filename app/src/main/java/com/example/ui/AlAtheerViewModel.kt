package com.example.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.AppRepository
import com.example.data.Attendance
import com.example.data.BackupEntity
import com.example.data.Group
import com.example.data.Payment
import com.example.data.Student
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AlAtheerViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: AppRepository

    init {
        val db = AppDatabase.getDatabase(application)
        repository = AppRepository(application, db)
    }

    // --- Search Queries ---
    val studentSearchQuery = MutableStateFlow("")
    val groupSearchQuery = MutableStateFlow("")

    // --- Core Data Flows ---
    val groups: StateFlow<List<Group>> = repository.allGroups
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val students: StateFlow<List<Student>> = repository.allStudents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val attendance: StateFlow<List<Attendance>> = repository.allAttendance
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val payments: StateFlow<List<Payment>> = repository.allPayments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val backups: StateFlow<List<BackupEntity>> = repository.allBackups
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Theme Settings Flow ---
    val isDarkMode = MutableStateFlow(false)

    // --- Cached student counts per group ---
    private val _groupStudentCounts = MutableStateFlow<Map<Int, Int>>(emptyMap())
    val groupStudentCounts: StateFlow<Map<Int, Int>> = _groupStudentCounts

    init {
        // Load settings
        viewModelScope.launch {
            val darkModeSetting = repository.getSettingSync("dark_mode")
            isDarkMode.value = darkModeSetting == "true"
            updateGroupStudentCounts()
        }

        // Keep group student counts synchronized with students changes
        viewModelScope.launch {
            students.collect {
                updateGroupStudentCounts()
            }
        }
    }

    private suspend fun updateGroupStudentCounts() {
        val currentStudents = students.value
        val counts = currentStudents.groupBy { it.groupId }.mapValues { it.value.size }
        _groupStudentCounts.value = counts
    }

    // --- Groups CRUD ---
    fun addGroup(name: String, stage: String, days: String, time: String, fees: Double, notes: String = "") {
        viewModelScope.launch {
            repository.insertGroup(Group(name = name, stage = stage, days = days, time = time, fees = fees, notes = notes))
        }
    }

    fun updateGroup(group: Group) {
        viewModelScope.launch {
            repository.updateGroup(group)
            updateGroupStudentCounts()
        }
    }

    fun deleteGroup(groupId: Int) {
        viewModelScope.launch {
            repository.deleteGroup(groupId)
            updateGroupStudentCounts()
        }
    }

    // --- Students CRUD ---
    fun addStudent(name: String, parentPhone: String, groupId: Int, joinDate: String, notes: String = "") {
        viewModelScope.launch {
            repository.insertStudent(Student(name = name, parentPhone = parentPhone, groupId = groupId, joinDate = joinDate, notes = notes))
            updateGroupStudentCounts()
        }
    }

    fun updateStudent(student: Student) {
        viewModelScope.launch {
            repository.updateStudent(student)
            updateGroupStudentCounts()
        }
    }

    fun deleteStudent(studentId: Int) {
        viewModelScope.launch {
            repository.deleteStudent(studentId)
            updateGroupStudentCounts()
        }
    }

    fun moveStudentToGroup(studentId: Int, newGroupId: Int) {
        viewModelScope.launch {
            val student = repository.getStudent(studentId).firstOrNull() ?: return@launch
            repository.updateStudent(student.copy(groupId = newGroupId))
            updateGroupStudentCounts()
        }
    }

    // --- Attendance Operations ---
    fun getAttendanceByGroupAndDateFlow(groupId: Int, date: String) =
        repository.getAttendanceByGroupAndDate(groupId, date)

    fun getStudentsWithAttendance(groupId: Int, date: String): StateFlow<List<Pair<Student, String>>> {
        val flow = MutableStateFlow<List<Pair<Student, String>>>(emptyList())
        viewModelScope.launch {
            combine(
                repository.getStudentsInGroup(groupId),
                repository.getAttendanceByGroupAndDate(groupId, date)
            ) { groupStudents, attendanceList ->
                groupStudents.map { student ->
                    val status = attendanceList.find { it.studentId == student.id }?.status ?: "UNRECORDED"
                    student to status
                }
            }.collect {
                flow.value = it
            }
        }
        return flow
    }

    fun updateStudentAttendance(studentId: Int, groupId: Int, date: String, status: String) {
        viewModelScope.launch {
            val existing = repository.getAttendanceByGroupAndDateList(groupId, date)
            val currentList = existing.toMutableList()
            
            val index = currentList.indexOfFirst { it.studentId == studentId }
            if (index != -1) {
                currentList[index] = currentList[index].copy(status = status)
            } else {
                currentList.add(Attendance(studentId = studentId, groupId = groupId, date = date, status = status))
            }
            repository.saveAttendance(currentList)
        }
    }

    // --- Payments Operations ---
    fun getStudentsWithPayment(groupId: Int, month: String): StateFlow<List<Pair<Student, Payment?>>> {
        val flow = MutableStateFlow<List<Pair<Student, Payment?>>>(emptyList())
        viewModelScope.launch {
            combine(
                repository.getStudentsInGroup(groupId),
                repository.getPaymentsByGroupAndMonth(groupId, month)
            ) { groupStudents, paymentList ->
                groupStudents.map { student ->
                    val payment = paymentList.find { it.studentId == student.id }
                    student to payment
                }
            }.collect {
                flow.value = it
            }
        }
        return flow
    }

    fun toggleStudentPayment(studentId: Int, groupId: Int, month: String, currentPayment: Payment?, fees: Double) {
        viewModelScope.launch {
            if (currentPayment == null) {
                repository.insertPayment(Payment(
                    studentId = studentId,
                    groupId = groupId,
                    month = month,
                    status = "PAID",
                    amountPaid = fees
                ))
            } else {
                val newStatus = if (currentPayment.status == "PAID") "UNPAID" else "PAID"
                val newAmount = if (newStatus == "PAID") fees else 0.0
                repository.insertPayment(currentPayment.copy(status = newStatus, amountPaid = newAmount))
            }
        }
    }

    // --- Settings & Backups ---
    fun setDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            isDarkMode.value = enabled
            repository.saveSetting("dark_mode", enabled.toString())
        }
    }

    fun backupDatabase(onComplete: (File?) -> Unit) {
        viewModelScope.launch {
            val file = repository.performBackup("MANUAL")
            onComplete(file)
        }
    }

    fun restoreDatabase(filePath: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = repository.restoreBackup(filePath)
            if (success) {
                updateGroupStudentCounts()
            }
            onComplete(success)
        }
    }

    fun restoreDatabaseFromUri(uri: Uri, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = repository.restoreFromUri(uri)
            if (success) {
                updateGroupStudentCounts()
            }
            onComplete(success)
        }
    }

    fun resetDatabase(onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.resetDatabase()
            updateGroupStudentCounts()
            onComplete()
        }
    }
}

class AlAtheerViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AlAtheerViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AlAtheerViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
