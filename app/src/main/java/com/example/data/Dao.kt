package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface GroupDao {
    @Query("SELECT * FROM groups ORDER BY name ASC")
    fun getAllGroups(): Flow<List<Group>>

    @Query("SELECT * FROM groups WHERE id = :id")
    fun getGroupByIdFlow(id: Int): Flow<Group?>

    @Query("SELECT * FROM groups WHERE id = :id")
    suspend fun getGroupById(id: Int): Group?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroup(group: Group): Long

    @Update
    suspend fun updateGroup(group: Group)

    @Delete
    suspend fun deleteGroup(group: Group)

    @Query("DELETE FROM groups WHERE id = :id")
    suspend fun deleteGroupById(id: Int)
}

@Dao
interface StudentDao {
    @Query("SELECT * FROM students ORDER BY name ASC")
    fun getAllStudents(): Flow<List<Student>>

    @Query("SELECT * FROM students WHERE id = :id")
    fun getStudentByIdFlow(id: Int): Flow<Student?>

    @Query("SELECT * FROM students WHERE id = :id")
    suspend fun getStudentById(id: Int): Student?

    @Query("SELECT * FROM students WHERE groupId = :groupId ORDER BY name ASC")
    fun getStudentsByGroup(groupId: Int): Flow<List<Student>>

    @Query("SELECT * FROM students WHERE groupId = :groupId ORDER BY name ASC")
    suspend fun getStudentsByGroupList(groupId: Int): List<Student>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudent(student: Student): Long

    @Update
    suspend fun updateStudent(student: Student)

    @Delete
    suspend fun deleteStudent(student: Student)

    @Query("DELETE FROM students WHERE id = :id")
    suspend fun deleteStudentById(id: Int)

    @Query("SELECT COUNT(*) FROM students WHERE groupId = :groupId")
    fun getStudentCountForGroup(groupId: Int): Flow<Int>

    @Query("SELECT COUNT(*) FROM students WHERE groupId = :groupId")
    suspend fun getStudentCountForGroupSync(groupId: Int): Int
}

@Dao
interface AttendanceDao {
    @Query("SELECT * FROM attendance ORDER BY date DESC")
    fun getAllAttendance(): Flow<List<Attendance>>

    @Query("SELECT * FROM attendance WHERE studentId = :studentId ORDER BY date DESC")
    fun getAttendanceForStudent(studentId: Int): Flow<List<Attendance>>

    @Query("SELECT * FROM attendance WHERE groupId = :groupId AND date = :date")
    fun getAttendanceByGroupAndDate(groupId: Int, date: String): Flow<List<Attendance>>

    @Query("SELECT * FROM attendance WHERE groupId = :groupId AND date = :date")
    suspend fun getAttendanceByGroupAndDateList(groupId: Int, date: String): List<Attendance>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendance(attendance: Attendance): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendanceList(attendanceList: List<Attendance>)

    @Query("DELETE FROM attendance WHERE studentId = :studentId")
    suspend fun deleteAttendanceForStudent(studentId: Int)

    @Query("DELETE FROM attendance WHERE groupId = :groupId AND date = :date")
    suspend fun deleteAttendanceForGroupAndDate(groupId: Int, date: String)
}

@Dao
interface PaymentDao {
    @Query("SELECT * FROM payments")
    fun getAllPayments(): Flow<List<Payment>>

    @Query("SELECT * FROM payments WHERE studentId = :studentId")
    fun getPaymentsForStudent(studentId: Int): Flow<List<Payment>>

    @Query("SELECT * FROM payments WHERE groupId = :groupId AND month = :month")
    fun getPaymentsByGroupAndMonth(groupId: Int, month: String): Flow<List<Payment>>

    @Query("SELECT * FROM payments WHERE groupId = :groupId AND month = :month")
    suspend fun getPaymentsByGroupAndMonthList(groupId: Int, month: String): List<Payment>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: Payment): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPaymentList(payments: List<Payment>)

    @Query("DELETE FROM payments WHERE studentId = :studentId")
    suspend fun deletePaymentsForStudent(studentId: Int)
}

@Dao
interface SettingsDao {
    @Query("SELECT * FROM settings WHERE `key` = :key")
    fun getSettingFlow(key: String): Flow<SettingsEntity?>

    @Query("SELECT * FROM settings WHERE `key` = :key")
    suspend fun getSetting(key: String): SettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSetting(setting: SettingsEntity)
}

@Dao
interface BackupDao {
    @Query("SELECT * FROM backup ORDER BY date DESC")
    fun getAllBackups(): Flow<List<BackupEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBackup(backup: BackupEntity): Long
}
