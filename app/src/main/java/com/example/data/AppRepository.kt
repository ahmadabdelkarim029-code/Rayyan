package com.example.data

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.flow.Flow
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AppRepository(private val context: Context, private val db: AppDatabase) {
    private val groupDao = db.groupDao()
    private val studentDao = db.studentDao()
    private val attendanceDao = db.attendanceDao()
    private val paymentDao = db.paymentDao()
    private val settingsDao = db.settingsDao()
    private val backupDao = db.backupDao()

    // --- Groups ---
    val allGroups: Flow<List<Group>> = groupDao.getAllGroups()
    fun getGroup(id: Int): Flow<Group?> = groupDao.getGroupByIdFlow(id)
    suspend fun getGroupSync(id: Int): Group? = groupDao.getGroupById(id)
    suspend fun insertGroup(group: Group): Long = groupDao.insertGroup(group)
    suspend fun updateGroup(group: Group) = groupDao.updateGroup(group)
    suspend fun deleteGroup(id: Int) = groupDao.deleteGroupById(id)

    // --- Students ---
    val allStudents: Flow<List<Student>> = studentDao.getAllStudents()
    fun getStudent(id: Int): Flow<Student?> = studentDao.getStudentByIdFlow(id)
    suspend fun getStudentSync(id: Int): Student? = studentDao.getStudentById(id)
    fun getStudentsInGroup(groupId: Int): Flow<List<Student>> = studentDao.getStudentsByGroup(groupId)
    suspend fun getStudentsInGroupList(groupId: Int): List<Student> = studentDao.getStudentsByGroupList(groupId)
    suspend fun insertStudent(student: Student): Long = studentDao.insertStudent(student)
    suspend fun updateStudent(student: Student) = studentDao.updateStudent(student)
    suspend fun deleteStudent(id: Int) = studentDao.deleteStudentById(id)
    fun getStudentCount(groupId: Int): Flow<Int> = studentDao.getStudentCountForGroup(groupId)
    suspend fun getStudentCountSync(groupId: Int): Int = studentDao.getStudentCountForGroupSync(groupId)

    // --- Attendance ---
    val allAttendance: Flow<List<Attendance>> = attendanceDao.getAllAttendance()
    fun getAttendanceForStudent(studentId: Int): Flow<List<Attendance>> = attendanceDao.getAttendanceForStudent(studentId)
    fun getAttendanceByGroupAndDate(groupId: Int, date: String): Flow<List<Attendance>> = attendanceDao.getAttendanceByGroupAndDate(groupId, date)
    suspend fun getAttendanceByGroupAndDateList(groupId: Int, date: String): List<Attendance> = attendanceDao.getAttendanceByGroupAndDateList(groupId, date)
    suspend fun saveAttendance(list: List<Attendance>) {
        if (list.isNotEmpty()) {
            val groupId = list[0].groupId
            val date = list[0].date
            attendanceDao.deleteAttendanceForGroupAndDate(groupId, date)
            attendanceDao.insertAttendanceList(list)
        }
    }

    // --- Payments ---
    val allPayments: Flow<List<Payment>> = paymentDao.getAllPayments()
    fun getPaymentsForStudent(studentId: Int): Flow<List<Payment>> = paymentDao.getPaymentsForStudent(studentId)
    fun getPaymentsByGroupAndMonth(groupId: Int, month: String): Flow<List<Payment>> = paymentDao.getPaymentsByGroupAndMonth(groupId, month)
    suspend fun getPaymentsByGroupAndMonthList(groupId: Int, month: String): List<Payment> = paymentDao.getPaymentsByGroupAndMonthList(groupId, month)
    suspend fun savePayments(list: List<Payment>) {
        paymentDao.insertPaymentList(list)
    }
    suspend fun insertPayment(payment: Payment) {
        paymentDao.insertPayment(payment)
    }

    // --- Settings ---
    fun getSetting(key: String): Flow<SettingsEntity?> = settingsDao.getSettingFlow(key)
    suspend fun getSettingSync(key: String): String? = settingsDao.getSetting(key)?.value
    suspend fun saveSetting(key: String, value: String) {
        settingsDao.insertSetting(SettingsEntity(key, value))
    }

    // --- Backups ---
    val allBackups: Flow<List<BackupEntity>> = backupDao.getAllBackups()

    suspend fun resetDatabase() {
        db.clearAllTables()
    }

    suspend fun performBackup(type: String = "MANUAL"): File? {
        return try {
            // Close or checkpoint DB properly
            db.openHelper.writableDatabase.query("PRAGMA wal_checkpoint(FULL)")
            
            val dbFile = context.getDatabasePath("al_atheer_database")
            if (!dbFile.exists()) return null

            val backupDir = File(context.getExternalFilesDir(null), "backups")
            if (!backupDir.exists()) backupDir.mkdirs()

            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val backupFile = File(backupDir, "al_atheer_backup_$timestamp.db")

            FileInputStream(dbFile).use { input ->
                FileOutputStream(backupFile).use { output ->
                    input.copyTo(output)
                }
            }

            // Also copy WAL and SHM if they exist to prevent corruption
            val walFile = File(dbFile.path + "-wal")
            if (walFile.exists()) {
                val backupWal = File(backupFile.path + "-wal")
                FileInputStream(walFile).use { input ->
                    FileOutputStream(backupWal).use { output ->
                        input.copyTo(output)
                    }
                }
            }

            val shmFile = File(dbFile.path + "-shm")
            if (shmFile.exists()) {
                val backupShm = File(backupFile.path + "-shm")
                FileInputStream(shmFile).use { input ->
                    FileOutputStream(backupShm).use { output ->
                        input.copyTo(output)
                    }
                }
            }

            val dateStr = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
            backupDao.insertBackup(BackupEntity(date = dateStr, filePath = backupFile.absolutePath, type = type))
            backupFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun restoreBackup(filePath: String): Boolean {
        return try {
            val backupFile = File(filePath)
            if (!backupFile.exists()) return false

            val dbFile = context.getDatabasePath("al_atheer_database")

            db.close()

            FileInputStream(backupFile).use { input ->
                FileOutputStream(dbFile).use { output ->
                    input.copyTo(output)
                }
            }

            // Restore WAL and SHM if present
            val backupWal = File(filePath + "-wal")
            val walFile = File(dbFile.path + "-wal")
            if (backupWal.exists()) {
                FileInputStream(backupWal).use { input ->
                    FileOutputStream(walFile).use { output ->
                        input.copyTo(output)
                    }
                }
            } else if (walFile.exists()) {
                walFile.delete()
            }

            val backupShm = File(filePath + "-shm")
            val shmFile = File(dbFile.path + "-shm")
            if (backupShm.exists()) {
                FileInputStream(backupShm).use { input ->
                    FileOutputStream(shmFile).use { output ->
                        input.copyTo(output)
                    }
                }
            } else if (shmFile.exists()) {
                shmFile.delete()
            }

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun restoreFromUri(uri: Uri): Boolean {
        return try {
            val dbFile = context.getDatabasePath("al_atheer_database")
            db.close()
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(dbFile).use { output ->
                    input.copyTo(output)
                }
            }
            // Clean up WAL and SHM
            val walFile = File(dbFile.path + "-wal")
            if (walFile.exists()) walFile.delete()
            val shmFile = File(dbFile.path + "-shm")
            if (shmFile.exists()) shmFile.delete()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
