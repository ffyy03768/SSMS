package com.example.studentmanagermvcandrxjava.detail

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import cn.pedant.SweetAlert.SweetAlertDialog
import com.example.studentmanagermvcandrxjava.R
import com.example.studentmanagermvcandrxjava.addEdit.AddEditStudentActivity
import com.example.studentmanagermvcandrxjava.databinding.ActivityStudentDetailBinding
import com.example.studentmanagermvcandrxjava.databinding.DialogAddGradeBinding
import com.example.studentmanagermvcandrxjava.model.MainRepository
import com.example.studentmanagermvcandrxjava.model.api.CourseDto
import com.example.studentmanagermvcandrxjava.model.api.GradeRequest
import com.example.studentmanagermvcandrxjava.model.local.MyDatabase
import com.example.studentmanagermvcandrxjava.model.local.student.GradeEntity
import com.example.studentmanagermvcandrxjava.model.local.student.StudentEntity
import com.example.studentmanagermvcandrxjava.model.local.student.StudentWithGrades
import com.example.studentmanagermvcandrxjava.utils.ApiServiceSingleTon
import com.example.studentmanagermvcandrxjava.utils.AppViewModelFactory
import com.example.studentmanagermvcandrxjava.utils.asyncRequest
import com.example.studentmanagermvcandrxjava.utils.average
import com.example.studentmanagermvcandrxjava.utils.formatScore
import com.example.studentmanagermvcandrxjava.utils.gpa
import com.example.studentmanagermvcandrxjava.utils.showError
import com.example.studentmanagermvcandrxjava.utils.showToast
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import io.reactivex.CompletableObserver
import io.reactivex.SingleObserver
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

/** 学生详情页：展示一个学生的资料与全部课程成绩（一对多），并可增删改成绩、编辑/删除学生。 */
class StudentDetailActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_STUDENT_ID = "extra_student_id"
    }

    private lateinit var binding: ActivityStudentDetailBinding
    private lateinit var viewModel: StudentDetailViewModel
    private lateinit var adapter: GradeAdapter
    private val disposables = CompositeDisposable()

    private var studentId: Long = -1L
    private var currentStudent: StudentEntity? = null
    private var courses: List<CourseDto> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStudentDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        studentId = intent.getLongExtra(EXTRA_STUDENT_ID, -1L)
        if (studentId == -1L) {
            showToast("无效的学生")
            finish()
            return
        }

        val repository = MainRepository(
            ApiServiceSingleTon.apiService,
            MyDatabase.getDatabase(applicationContext).studentDao
        )
        viewModel = ViewModelProvider(this, AppViewModelFactory(repository))[StudentDetailViewModel::class.java]

        adapter = GradeAdapter(
            onClick = { grade -> showGradeDialog(grade) },
            onLongClick = { grade -> confirmDeleteGrade(grade) }
        )
        binding.recyclerGrades.layoutManager = LinearLayoutManager(this)
        binding.recyclerGrades.adapter = adapter

        viewModel.student(studentId).observe(this) { item ->
            if (item == null) {
                finish()
                return@observe
            }
            bindHeader(item)
            adapter.submitList(item.grades)
            binding.txtNoGrades.visibility = if (item.grades.isEmpty()) View.VISIBLE else View.GONE
        }

        loadCourses()
        binding.btnAddGrade.setOnClickListener { showGradeDialog(null) }
    }

    private fun bindHeader(item: StudentWithGrades) {
        val s = item.student
        currentStudent = s
        binding.txtAvatar.text = s.name.firstOrNull()?.uppercase() ?: "?"
        binding.txtName.text = s.name
        binding.txtMeta.text = "${s.studentNo} · ${s.major}\n${s.className} · ${s.enrollYear} 级"
        binding.txtAverage.text = if (item.grades.isEmpty()) "—" else item.average().toString()
        binding.txtGpa.text = if (item.grades.isEmpty()) "—" else item.gpa().toString()
        binding.txtCount.text = item.grades.size.toString()
    }

    private fun loadCourses() {
        viewModel.courses().asyncRequest().subscribe(object : SingleObserver<List<CourseDto>> {
            override fun onSubscribe(d: Disposable) { disposables.add(d) }
            override fun onSuccess(t: List<CourseDto>) { courses = t }
            override fun onError(e: Throwable) { /* 加载课程失败时仍可查看，添加成绩时再提示 */ }
        })
    }

    /** grade 为 null 表示新增成绩；否则为编辑已有成绩。 */
    private fun showGradeDialog(grade: GradeEntity?) {
        if (courses.isEmpty()) {
            showToast("课程列表为空，请确认后端 /courses 可访问")
            return
        }
        val dialogBinding = DialogAddGradeBinding.inflate(layoutInflater)
        val courseNames = courses.map { "${it.name}（${it.credit}学分）" }
        dialogBinding.dropdownCourse.setAdapter(
            ArrayAdapter(this, android.R.layout.simple_list_item_1, courseNames)
        )

        var selectedCourseId: Long = grade?.courseId ?: courses.first().id
        if (grade != null) {
            val idx = courses.indexOfFirst { it.id == grade.courseId }.coerceAtLeast(0)
            dialogBinding.dropdownCourse.setText(courseNames[idx], false)
            dialogBinding.edtScore.setText(formatScore(grade.score))
            dialogBinding.edtTerm.setText(grade.term)
        } else {
            dialogBinding.dropdownCourse.setText(courseNames.first(), false)
            dialogBinding.edtTerm.setText("2024-1")
        }
        dialogBinding.dropdownCourse.setOnItemClickListener { _, _, pos, _ ->
            selectedCourseId = courses[pos].id
        }

        MaterialAlertDialogBuilder(this)
            .setTitle(if (grade == null) "添加成绩" else "修改成绩")
            .setView(dialogBinding.root)
            .setNegativeButton("取消", null)
            .setPositiveButton(if (grade == null) "添加" else "保存") { _, _ ->
                val score = dialogBinding.edtScore.text?.toString()?.trim()?.toDoubleOrNull()
                val term = dialogBinding.edtTerm.text?.toString()?.trim().orEmpty()
                if (score == null) {
                    showToast("请输入有效的成绩")
                    return@setPositiveButton
                }
                val req = GradeRequest(selectedCourseId, score, term)
                val request = if (grade == null) viewModel.addGrade(studentId, req)
                else viewModel.updateGrade(grade.id, req)
                request.asyncRequest().subscribe(
                    completableObserver(if (grade == null) "已添加成绩" else "已修改成绩")
                )
            }
            .show()
    }

    private fun confirmDeleteGrade(grade: GradeEntity) {
        SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
            .setTitleText("删除成绩")
            .setContentText("确定删除「${grade.courseName}」的成绩吗？")
            .setCancelText("取消")
            .setConfirmText("删除")
            .setCancelClickListener { it.dismissWithAnimation() }
            .setConfirmClickListener {
                it.dismissWithAnimation()
                viewModel.deleteGrade(grade.id).asyncRequest()
                    .subscribe(completableObserver("已删除成绩"))
            }
            .show()
    }

    private fun confirmDeleteStudent() {
        SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
            .setTitleText("删除学生")
            .setContentText("确定删除该学生及其全部成绩吗？")
            .setCancelText("取消")
            .setConfirmText("删除")
            .setCancelClickListener { it.dismissWithAnimation() }
            .setConfirmClickListener {
                it.dismissWithAnimation()
                viewModel.deleteStudent(studentId).asyncRequest().subscribe(object : CompletableObserver {
                    override fun onSubscribe(d: Disposable) { disposables.add(d) }
                    override fun onComplete() { showToast("已删除"); finish() }
                    override fun onError(e: Throwable) { showError(e.message ?: "删除失败") }
                })
            }
            .show()
    }

    private fun completableObserver(successMsg: String) = object : CompletableObserver {
        override fun onSubscribe(d: Disposable) { disposables.add(d) }
        override fun onComplete() { showToast(successMsg) }
        override fun onError(e: Throwable) { showError(e.message ?: "操作失败") }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_detail, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
            R.id.action_edit -> currentStudent?.let {
                val intent = Intent(this, AddEditStudentActivity::class.java)
                intent.putExtra(AddEditStudentActivity.EXTRA_STUDENT, it)
                startActivity(intent)
            }
            R.id.action_delete -> confirmDeleteStudent()
        }
        return true
    }

    override fun onDestroy() {
        disposables.clear()
        super.onDestroy()
    }
}
