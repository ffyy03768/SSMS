package com.example.studentmanagermvcandrxjava.mainScreen

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.studentmanagermvcandrxjava.model.MainRepository
import com.example.studentmanagermvcandrxjava.model.local.student.StudentWithGrades
import io.reactivex.CompletableObserver
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

class MainScreenViewModel(
    private val repository: MainRepository
) : ViewModel() {

    private val disposables = CompositeDisposable()

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    /** 界面观察的列表数据（来自 Room）。 */
    val students: LiveData<List<StudentWithGrades>> = repository.observeStudents()

    init {
        refresh()
    }

    fun refresh() {
        _loading.postValue(true)
        repository.refresh()
            .subscribeOn(Schedulers.io())
            .subscribe(object : CompletableObserver {
                override fun onSubscribe(d: Disposable) { disposables.add(d) }
                override fun onComplete() { _loading.postValue(false) }
                override fun onError(e: Throwable) {
                    _loading.postValue(false)
                    _error.postValue(e.message ?: "网络请求失败")
                }
            })
    }

    override fun onCleared() {
        disposables.clear()
        super.onCleared()
    }
}
