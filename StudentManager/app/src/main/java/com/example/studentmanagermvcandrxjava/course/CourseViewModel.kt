package com.example.studentmanagermvcandrxjava.course

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.studentmanagermvcandrxjava.model.MainRepository
import com.example.studentmanagermvcandrxjava.model.api.CourseDto
import com.example.studentmanagermvcandrxjava.model.api.CourseRequest
import com.example.studentmanagermvcandrxjava.utils.asyncRequest
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.SingleObserver
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

/** 课程管理 ViewModel：维护课程列表，并提供增删改（增删改的成功/失败由界面处理后调用 load() 刷新）。 */
class CourseViewModel(private val repository: MainRepository) : ViewModel() {

    private val disposables = CompositeDisposable()

    private val _courses = MutableLiveData<List<CourseDto>>()
    val courses: LiveData<List<CourseDto>> = _courses

    private val _loadError = MutableLiveData<String?>()
    val loadError: LiveData<String?> = _loadError

    init { load() }

    fun load() {
        repository.getCourses().asyncRequest().subscribe(object : SingleObserver<List<CourseDto>> {
            override fun onSubscribe(d: Disposable) { disposables.add(d) }
            override fun onSuccess(t: List<CourseDto>) { _courses.value = t; _loadError.value = null }
            override fun onError(e: Throwable) { _loadError.value = e.message ?: "加载失败" }
        })
    }

    fun create(req: CourseRequest): Single<CourseDto> = repository.createCourse(req)
    fun update(id: Long, req: CourseRequest): Single<CourseDto> = repository.updateCourse(id, req)
    fun delete(id: Long): Completable = repository.deleteCourse(id)

    override fun onCleared() {
        disposables.clear()
        super.onCleared()
    }
}
