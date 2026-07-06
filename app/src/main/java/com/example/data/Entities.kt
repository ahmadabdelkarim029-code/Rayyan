package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "groups")
data class Group(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val stage: String, // المرحلة الدراسية
    val days: String,  // الأيام
    val time: String,  // الوقت
    val fees: Double,  // قيمة الاشتراك الشهري
    val notes: String = ""
)

@Entity(tableName = "students")
data class Student(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val parentPhone: String,
    val groupId: Int, // رقم المجموعة
    val joinDate: String, // تاريخ الانضمام
    val notes: String = ""
)

@Entity(tableName = "attendance")
data class Attendance(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val studentId: Int,
    val groupId: Int,
    val date: String, // صيغة YYYY-MM-DD
    val status: String // PRESENT (حاضر), ABSENT (غائب), LATE (متأخر)
)

@Entity(tableName = "payments")
data class Payment(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val studentId: Int,
    val groupId: Int,
    val month: String, // صيغة YYYY-MM
    val status: String, // PAID (تم الدفع), UNPAID (لم يدفع)
    val amountPaid: Double
)

@Entity(tableName = "settings")
data class SettingsEntity(
    @PrimaryKey val key: String,
    val value: String
)

@Entity(tableName = "backup")
data class BackupEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: String,
    val filePath: String,
    val type: String // AUTO, MANUAL
)
