package com.example.studentmanagermvcandrxjava.detail

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.studentmanagermvcandrxjava.model.MainRepository
import com.example.studentmanagermvcandrxjava.model.api.CourseDto
import com.example.studentmanagermvcandrxjava.model.api.GradeRequest
import com.example.studentmanagermvcandrxjava.model.local.student.StudentWithGrades
import io.reactivex.Completable
import io.reactivex.Single

class StudentDetailViewModel(
    private val repository: MainRepository
) : ViewModel() {

    fun student(id: Long): LiveData<StudentWithGrades> = repository.observeStudent(id)

    fun courses(): Single<List<CourseDto>> = repository.getCourses()

    fun addGrade(studentId: Long, req: GradeRequest): Completable = repository.addGrade(studentId, req)
    fun updateGrade(gradeId: Long, req: GradeRequest): Completable = repository.updateGrade(gradeId, req)
    fun deleteGrade(gradeId: Long): Completable = repository.deleteGrade(gradeId)
    fun deleteStudent(studentId: Long): Completable = repository.deleteStudent(studentId)
}
