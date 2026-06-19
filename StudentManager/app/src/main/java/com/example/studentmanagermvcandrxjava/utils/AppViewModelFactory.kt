package com.example.studentmanagermvcandrxjava.utils

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.studentmanagermvcandrxjava.addEdit.AddEditStudentViewModel
import com.example.studentmanagermvcandrxjava.course.CourseViewModel
import com.example.studentmanagermvcandrxjava.detail.StudentDetailViewModel
import com.example.studentmanagermvcandrxjava.mainScreen.MainScreenViewModel
import com.example.studentmanagermvcandrxjava.model.MainRepository
import com.example.studentmanagermvcandrxjava.stats.StatsViewModel

/** 统一的 ViewModel 工厂：按类型创建对应 ViewModel。 */
@Suppress("UNCHECKED_CAST")
class AppViewModelFactory(private val repository: MainRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T = when {
        modelClass.isAssignableFrom(MainScreenViewModel::class.java) -> MainScreenViewModel(repository)
        modelClass.isAssignableFrom(StudentDetailViewModel::class.java) -> StudentDetailViewModel(repository)
        modelClass.isAssignableFrom(AddEditStudentViewModel::class.java) -> AddEditStudentViewModel(repository)
        modelClass.isAssignableFrom(CourseViewModel::class.java) -> CourseViewModel(repository)
        modelClass.isAssignableFrom(StatsViewModel::class.java) -> StatsViewModel(repository)
        else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    } as T
}
