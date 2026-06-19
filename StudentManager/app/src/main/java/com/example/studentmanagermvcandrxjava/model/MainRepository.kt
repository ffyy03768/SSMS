package com.example.studentmanagermvcandrxjava.model

import androidx.lifecycle.LiveData
import com.example.studentmanagermvcandrxjava.model.api.ApiService
import com.example.studentmanagermvcandrxjava.model.api.CourseDto
import com.example.studentmanagermvcandrxjava.model.api.CourseRequest
import com.example.studentmanagermvcandrxjava.model.api.GradeRequest
import com.example.studentmanagermvcandrxjava.model.api.StudentDto
import com.example.studentmanagermvcandrxjava.model.api.StudentRequest
import com.example.studentmanagermvcandrxjava.model.local.student.GradeEntity
import com.example.studentmanagermvcandrxjava.model.local.student.StudentDao
import com.example.studentmanagermvcandrxjava.model.local.student.StudentEntity
import com.example.studentmanagermvcandrxjava.model.local.student.StudentWithGrades
import io.reactivex.Completable
import io.reactivex.Single

/**
 * 仓库层：协调「网络（Retrofit）」与「本地（Room）」。
 * 界面始终观察 Room（单一数据来源）；任何写操作成功后都会刷新本地，保证一致。
 */
class MainRepository(
    private val api: ApiService,
    private val dao: StudentDao
) {

    // -------- 观察本地数据 --------
    fun observeStudents(): LiveData<List<StudentWithGrades>> = dao.observeStudentsWithGrades()

    fun observeStudent(id: Long): LiveData<StudentWithGrades> = dao.observeStudentWithGrades(id)

    // -------- 从服务器刷新到本地 --------
    fun refresh(): Completable =
        api.getAllStudents()
            .doOnSuccess { cacheToLocal(it) }
            .ignoreElement()

    private fun cacheToLocal(dtos: List<StudentDto>) {
        val students = dtos.map {
            StudentEntity(it.id, it.studentNo, it.name, it.gender, it.major, it.className, it.enrollYear, it.email)
        }
        val grades = dtos.flatMap { s ->
            s.grades.map { g ->
                GradeEntity(g.id, s.id, g.courseId, g.courseName, g.credit, g.type, g.score, g.term)
            }
        }
        dao.replaceAll(students, grades)
    }

    // -------- 学生增删改（先服务器，成功后刷新本地） --------
    fun createStudent(req: StudentRequest): Completable =
        api.createStudent(req).flatMapCompletable { refresh() }

    fun updateStudent(id: Long, req: StudentRequest): Completable =
        api.updateStudent(id, req).flatMapCompletable { refresh() }

    fun deleteStudent(id: Long): Completable =
        api.deleteStudent(id).andThen(refresh())

    // -------- 成绩增删改 --------
    fun addGrade(studentId: Long, req: GradeRequest): Completable =
        api.addGrade(studentId, req).flatMapCompletable { refresh() }

    fun updateGrade(gradeId: Long, req: GradeRequest): Completable =
        api.updateGrade(gradeId, req).flatMapCompletable { refresh() }

    fun deleteGrade(gradeId: Long): Completable =
        api.deleteGrade(gradeId).andThen(refresh())

    // -------- 课程 --------
    fun getCourses(): Single<List<CourseDto>> = api.getCourses()
    fun createCourse(req: CourseRequest): Single<CourseDto> = api.createCourse(req)
    fun updateCourse(id: Long, req: CourseRequest): Single<CourseDto> = api.updateCourse(id, req)
    fun deleteCourse(id: Long): Completable = api.deleteCourse(id)
}
