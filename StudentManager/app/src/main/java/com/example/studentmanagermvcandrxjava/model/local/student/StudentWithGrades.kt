package com.example.studentmanagermvcandrxjava.model.local.student

import androidx.room.Embedded
import androidx.room.Relation

/**
 * 一对多查询结果：一个学生 + 它的全部成绩。
 * Room 会根据 studentId 自动把成绩归并到对应学生名下。
 */
data class StudentWithGrades(
    @Embedded val student: StudentEntity,
    @Relation(parentColumn = "id", entityColumn = "studentId")
    val grades: List<GradeEntity>
)
