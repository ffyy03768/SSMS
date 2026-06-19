package com.example.studentmanagermvcandrxjava.addEdit

import androidx.lifecycle.ViewModel
import com.example.studentmanagermvcandrxjava.model.MainRepository
import com.example.studentmanagermvcandrxjava.model.api.StudentRequest
import io.reactivex.Completable

class AddEditStudentViewModel(
    private val repository: MainRepository
) : ViewModel() {
    fun create(req: StudentRequest): Completable = repository.createStudent(req)
    fun update(id: Long, req: StudentRequest): Completable = repository.updateStudent(id, req)
}
