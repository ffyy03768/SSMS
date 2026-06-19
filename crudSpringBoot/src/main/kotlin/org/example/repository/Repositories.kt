package org.example.repository

import org.example.entity.Course
import org.example.entity.Grade
import org.example.entity.Student
import org.springframework.data.jpa.repository.JpaRepository

/**
 * Spring Data JPA 仓库接口。
 * 继承 JpaRepository 后即自动拥有 findAll / findById / save / deleteById 等方法，
 * 无需手写 SQL。
 */
interface StudentRepository : JpaRepository<Student, Long>

interface CourseRepository : JpaRepository<Course, Long>

interface GradeRepository : JpaRepository<Grade, Long> {
    /** 是否存在引用了某门课程的成绩（删课程前的安全检查）。 */
    fun existsByCourse_Id(courseId: Long): Boolean
}
