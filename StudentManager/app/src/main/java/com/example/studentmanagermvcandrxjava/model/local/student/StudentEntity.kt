package com.example.studentmanagermvcandrxjava.model.local.student

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

/** 本地 Room 学生表。作为离线缓存，也是界面的「单一数据来源」。 */
@Parcelize
@Entity(tableName = "student_table")
data class StudentEntity(
    @PrimaryKey val id: Long,
    val studentNo: String,
    val name: String,
    val gender: String,
    val major: String,
    val className: String,
    val enrollYear: Int,
    val email: String
) : Parcelable
