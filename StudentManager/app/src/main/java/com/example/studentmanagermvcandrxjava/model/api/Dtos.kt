package com.example.studentmanagermvcandrxjava.model.api

/**
 * 与后端 REST 接口对应的网络数据模型（由 Gson 解析）。
 * 字段名与后端返回的 JSON 完全一致（camelCase）。
 */

// ---------- 响应 ----------
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
    val grades: List<GradeDto>
)

data class GradeDto(
    val id: Long,
    val courseId: Long,
    val courseName: String,
    val credit: Int,
    val type: String,
    val score: Double,
    val term: String
)

data class CourseDto(
    val id: Long,
    val name: String,
    val credit: Int,
    val type: String
)

// ---------- 请求 ----------
data class StudentRequest(
    val studentNo: String,
    val name: String,
    val gender: String,
    val major: String,
    val className: String,
    val enrollYear: Int,
    val email: String
)

data class GradeRequest(
    val courseId: Long,
    val score: Double,
    val term: String
)

data class CourseRequest(
    val name: String,
    val credit: Int,
    val type: String
)
