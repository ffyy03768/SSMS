package com.example.studentmanagermvcandrxjava.model.local.student

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 本地 Room 成绩表，是学生的「多」端。
 * 这里把课程名 / 学分 / 类型一并缓存下来，方便离线展示，无需再做表连接。
 */
@Entity(tableName = "grade_table")
data class GradeEntity(
    @PrimaryKey val id: Long,
    val studentId: Long,   // 指向 StudentEntity.id（一对多的外键概念）
    val courseId: Long,
    val courseName: String,
    val credit: Int,
    val type: String,
    val score: Double,
    val term: String
)
