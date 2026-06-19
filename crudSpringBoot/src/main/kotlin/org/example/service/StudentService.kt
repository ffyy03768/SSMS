package org.example.service

import org.example.dto.*
import org.example.entity.Grade
import org.example.entity.Student
import org.example.repository.CourseRepository
import org.example.repository.GradeRepository
import org.example.repository.StudentRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 业务逻辑层：负责增删改查、组合一对多数据、并把实体转换成 DTO。
 * 用 @Transactional 保证读取关联集合（grades）时事务仍然开启。
 */
@Service
@Transactional
class StudentService(
    private val studentRepo: StudentRepository,
    private val courseRepo: CourseRepository,
    private val gradeRepo: GradeRepository
) {

    @Transactional(readOnly = true)
    fun findAll(): List<StudentDto> = studentRepo.findAll().map { it.toDto() }

    @Transactional(readOnly = true)
    fun findOne(id: Long): StudentDto =
        studentRepo.findById(id).orElseThrow { NoSuchElementException("学生不存在: id=$id") }.toDto()

    fun create(req: StudentRequest): StudentDto {
        val s = Student(
            studentNo = req.studentNo, name = req.name, gender = req.gender,
            major = req.major, className = req.className,
            enrollYear = req.enrollYear, email = req.email
        )
        return studentRepo.save(s).toDto()
    }

    fun update(id: Long, req: StudentRequest): StudentDto {
        val s = studentRepo.findById(id).orElseThrow { NoSuchElementException("学生不存在: id=$id") }
        s.studentNo = req.studentNo
        s.name = req.name
        s.gender = req.gender
        s.major = req.major
        s.className = req.className
        s.enrollYear = req.enrollYear
        s.email = req.email
        return studentRepo.save(s).toDto()
    }

    fun delete(id: Long) = studentRepo.deleteById(id)

    // ---------- 成绩相关 ----------

    fun addGrade(studentId: Long, req: GradeRequest): GradeDto {
        val student = studentRepo.findById(studentId)
            .orElseThrow { NoSuchElementException("学生不存在: id=$studentId") }
        val course = courseRepo.findById(req.courseId)
            .orElseThrow { NoSuchElementException("课程不存在: id=${req.courseId}") }
        val grade = Grade(student = student, course = course, score = req.score, term = req.term)
        return gradeRepo.save(grade).toDto()
    }

    fun updateGrade(gradeId: Long, req: GradeRequest): GradeDto {
        val grade = gradeRepo.findById(gradeId)
            .orElseThrow { NoSuchElementException("成绩不存在: id=$gradeId") }
        val course = courseRepo.findById(req.courseId)
            .orElseThrow { NoSuchElementException("课程不存在: id=${req.courseId}") }
        grade.course = course
        grade.score = req.score
        grade.term = req.term
        return gradeRepo.save(grade).toDto()
    }

    fun deleteGrade(gradeId: Long) = gradeRepo.deleteById(gradeId)
}
