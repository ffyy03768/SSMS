package org.example.dto

import org.example.entity.Course
import org.example.entity.Grade
import org.example.entity.Student

// ============ 响应 DTO（返回给前端 / 页面）============

/** 单条成绩，附带它所属课程的信息，方便前端直接展示。 */
data class GradeDto(
    val id: Long,
    val courseId: Long,
    val courseName: String,
    val credit: Int,
    val type: String,
    val score: Double,
    val term: String
)

/** 学生 + 它的全部成绩 + 统计值（课程数、平均分）。 */
data class StudentDto(
    val id: Long,
    val studentNo: String,
    val name: String,
    val gender: String,
    val major: String,
    val className: String,
    val enrollYear: Int,
    val email: String,
    val courseCount: Int,
    val average: Double,
    val gpa: Double,
    val grades: List<GradeDto>
)

data class CourseDto(
    val id: Long,
    val name: String,
    val credit: Int,
    val type: String
)

// ============ 请求 DTO（前端传入）============

data class StudentRequest(
    val studentNo: String,
    val name: String,
    val gender: String = "",
    val major: String = "",
    val className: String = "",
    val enrollYear: Int = 0,
    val email: String = ""
)

data class GradeRequest(
    val courseId: Long,
    val score: Double,
    val term: String = ""
)

data class CourseRequest(
    val name: String,
    val credit: Int,
    val type: String = "必修"
)

// ============ 实体 → DTO 的映射扩展函数 ============

fun Grade.toDto() = GradeDto(
    id = id,
    courseId = course?.id ?: 0,
    courseName = course?.name ?: "",
    credit = course?.credit ?: 0,
    type = course?.type ?: "",
    score = score,
    term = term
)

fun Student.toDto(): StudentDto {
    val gradeDtos = grades.map { it.toDto() }
    // 平均分保留 1 位小数；没有成绩时为 0.0
    val avg = if (gradeDtos.isEmpty()) 0.0
    else Math.round(gradeDtos.map { it.score }.average() * 10) / 10.0
    // 绩点：学分加权平均分换算 (加权平均分 − 50) / 10，保留 2 位小数
    val totalCredit = gradeDtos.sumOf { it.credit }
    val gpa = if (gradeDtos.isEmpty() || totalCredit == 0) 0.0
    else {
        val weighted = gradeDtos.sumOf { it.score * it.credit } / totalCredit
        Math.round((weighted - 50) / 10.0 * 100) / 100.0
    }
    return StudentDto(
        id = id, studentNo = studentNo, name = name, gender = gender,
        major = major, className = className, enrollYear = enrollYear, email = email,
        courseCount = gradeDtos.size, average = avg, gpa = gpa, grades = gradeDtos
    )
}

fun Course.toDto() = CourseDto(id, name, credit, type)
