package com.example.studentmanagermvcandrxjava.stats

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.studentmanagermvcandrxjava.model.MainRepository
import com.example.studentmanagermvcandrxjava.model.api.CourseDto
import com.example.studentmanagermvcandrxjava.model.local.student.StudentWithGrades
import com.example.studentmanagermvcandrxjava.utils.asyncRequest
import io.reactivex.CompletableObserver
import io.reactivex.SingleObserver
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

/** 统计页 ViewModel：观察本地学生数据用于排名/分布，并单独取课程总数。 */
class StatsViewModel(private val repository: MainRepository) : ViewModel() {

    private val disposables = CompositeDisposable()

    /** 排名与分布都基于本地缓存的学生数据。 */
    val students: LiveData<List<StudentWithGrades>> = repository.observeStudents()

    private val _courseCount = MutableLiveData(0)
    val courseCount: LiveData<Int> = _courseCount

    init {
        refresh()
        loadCourseCount()
    }

    /** 进入页面时顺带从服务器刷新一次，保证统计是最新的。 */
    fun refresh() {
        repository.refresh().subscribeOn(Schedulers.io()).subscribe(object : CompletableObserver {
            override fun onSubscribe(d: Disposable) { disposables.add(d) }
            override fun onComplete() {}
            override fun onError(e: Throwable) {}
        })
    }

    private fun loadCourseCount() {
        repository.getCourses().asyncRequest().subscribe(object : SingleObserver<List<CourseDto>> {
            override fun onSubscribe(d: Disposable) { disposables.add(d) }
            override fun onSuccess(t: List<CourseDto>) { _courseCount.value = t.size }
            override fun onError(e: Throwable) {}
        })
    }

    override fun onCleared() {
        disposables.clear()
        super.onCleared()
    }
}
