package com.example.studentmanagermvcandrxjava.course

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import cn.pedant.SweetAlert.SweetAlertDialog
import com.example.studentmanagermvcandrxjava.databinding.ActivityCourseListBinding
import com.example.studentmanagermvcandrxjava.databinding.DialogAddCourseBinding
import com.example.studentmanagermvcandrxjava.model.MainRepository
import com.example.studentmanagermvcandrxjava.model.api.CourseDto
import com.example.studentmanagermvcandrxjava.model.api.CourseRequest
import com.example.studentmanagermvcandrxjava.model.local.MyDatabase
import com.example.studentmanagermvcandrxjava.utils.ApiServiceSingleTon
import com.example.studentmanagermvcandrxjava.utils.AppViewModelFactory
import com.example.studentmanagermvcandrxjava.utils.asyncRequest
import com.example.studentmanagermvcandrxjava.utils.showError
import com.example.studentmanagermvcandrxjava.utils.showToast
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import io.reactivex.CompletableObserver
import io.reactivex.SingleObserver
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import retrofit2.HttpException

/** 课程管理页：列出全部课程，支持新增、点击编辑、长按删除。 */
class CourseListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCourseListBinding
    private lateinit var viewModel: CourseViewModel
    private lateinit var adapter: CourseAdapter
    private val disposables = CompositeDisposable()

    private val types = listOf("必修", "选修")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCourseListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        val repository = MainRepository(
            ApiServiceSingleTon.apiService,
            MyDatabase.getDatabase(applicationContext).studentDao
        )
        viewModel = ViewModelProvider(this, AppViewModelFactory(repository))[CourseViewModel::class.java]

        adapter = CourseAdapter(
            onClick = { showCourseDialog(it) },
            onLongClick = { confirmDelete(it) }
        )
        binding.recyclerCourses.layoutManager = LinearLayoutManager(this)
        binding.recyclerCourses.adapter = adapter

        viewModel.courses.observe(this) { list ->
            adapter.submitList(list)
            binding.txtEmptyCourses.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
        }
        viewModel.loadError.observe(this) { msg ->
            if (msg != null) binding.txtEmptyCourses.apply {
                text = "加载失败：$msg"; visibility = View.VISIBLE
            }
        }

        binding.btnAddCourse.setOnClickListener { showCourseDialog(null) }
    }

    /** course 为 null 表示新增；否则编辑。 */
    private fun showCourseDialog(course: CourseDto?) {
        val dialogBinding = DialogAddCourseBinding.inflate(layoutInflater)
        dialogBinding.dropdownType.setAdapter(
            ArrayAdapter(this, android.R.layout.simple_list_item_1, types)
        )
        if (course != null) {
            dialogBinding.edtCourseName.setText(course.name)
            dialogBinding.edtCredit.setText(course.credit.toString())
            dialogBinding.dropdownType.setText(course.type.ifBlank { types.first() }, false)
        } else {
            dialogBinding.dropdownType.setText(types.first(), false)
        }

        MaterialAlertDialogBuilder(this)
            .setTitle(if (course == null) getString(com.example.studentmanagermvcandrxjava.R.string.add_course)
            else getString(com.example.studentmanagermvcandrxjava.R.string.edit_course))
            .setView(dialogBinding.root)
            .setNegativeButton("取消", null)
            .setPositiveButton(if (course == null) "添加" else "保存") { _, _ ->
                val name = dialogBinding.edtCourseName.text?.toString()?.trim().orEmpty()
                val credit = dialogBinding.edtCredit.text?.toString()?.trim()?.toIntOrNull()
                val type = dialogBinding.dropdownType.text?.toString()?.trim().orEmpty().ifBlank { "必修" }
                if (name.isEmpty()) { showToast("请输入课程名称"); return@setPositiveButton }
                if (credit == null || credit < 0) { showToast("请输入有效学分"); return@setPositiveButton }
                val req = CourseRequest(name, credit, type)
                val request = if (course == null) viewModel.create(req) else viewModel.update(course.id, req)
                request.asyncRequest().subscribe(object : SingleObserver<CourseDto> {
                    override fun onSubscribe(d: Disposable) { disposables.add(d) }
                    override fun onSuccess(t: CourseDto) {
                        showToast(if (course == null) "已添加课程" else "已保存")
                        viewModel.load()
                    }
                    override fun onError(e: Throwable) { showError(e.message ?: "操作失败") }
                })
            }
            .show()
    }

    private fun confirmDelete(course: CourseDto) {
        SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
            .setTitleText("删除课程")
            .setContentText("确定删除「${course.name}」吗？")
            .setCancelText("取消")
            .setConfirmText("删除")
            .setCancelClickListener { it.dismissWithAnimation() }
            .setConfirmClickListener {
                it.dismissWithAnimation()
                viewModel.delete(course.id).asyncRequest().subscribe(object : CompletableObserver {
                    override fun onSubscribe(d: Disposable) { disposables.add(d) }
                    override fun onComplete() { showToast("已删除课程"); viewModel.load() }
                    override fun onError(e: Throwable) {
                        val msg = if (e is HttpException && e.code() == 409)
                            "该课程已有成绩记录，无法删除" else (e.message ?: "删除失败")
                        showError(msg)
                    }
                })
            }
            .show()
    }

    override fun onDestroy() {
        disposables.clear()
        super.onDestroy()
    }
}
