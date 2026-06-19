package com.example.studentmanagermvcandrxjava.addEdit

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.studentmanagermvcandrxjava.databinding.ActivityAddEditStudentBinding
import com.example.studentmanagermvcandrxjava.model.MainRepository
import com.example.studentmanagermvcandrxjava.model.api.StudentRequest
import com.example.studentmanagermvcandrxjava.model.local.MyDatabase
import com.example.studentmanagermvcandrxjava.model.local.student.StudentEntity
import com.example.studentmanagermvcandrxjava.utils.ApiServiceSingleTon
import com.example.studentmanagermvcandrxjava.utils.AppViewModelFactory
import com.example.studentmanagermvcandrxjava.utils.asyncRequest
import com.example.studentmanagermvcandrxjava.utils.showError
import com.example.studentmanagermvcandrxjava.utils.showToast
import io.reactivex.CompletableObserver
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

/** 添加 / 编辑学生页：填写学生基本资料（学号、姓名、专业、班级等）。 */
class AddEditStudentActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_STUDENT = "extra_student"
    }

    private lateinit var binding: ActivityAddEditStudentBinding
    private lateinit var viewModel: AddEditStudentViewModel
    private val disposables = CompositeDisposable()

    private var editing: StudentEntity? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddEditStudentBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        val repository = MainRepository(
            ApiServiceSingleTon.apiService,
            MyDatabase.getDatabase(applicationContext).studentDao
        )
        viewModel = ViewModelProvider(this, AppViewModelFactory(repository))[AddEditStudentViewModel::class.java]

        @Suppress("DEPRECATION")
        editing = intent.getParcelableExtra(EXTRA_STUDENT)
        if (editing != null) {
            supportActionBar?.title = "编辑学生"
            fillForm(editing!!)
        } else {
            supportActionBar?.title = "添加学生"
        }

        binding.btnSave.setOnClickListener { save() }
    }

    private fun fillForm(s: StudentEntity) {
        binding.edtStudentNo.setText(s.studentNo)
        binding.edtName.setText(s.name)
        binding.edtGender.setText(s.gender)
        binding.edtMajor.setText(s.major)
        binding.edtClassName.setText(s.className)
        binding.edtEnrollYear.setText(if (s.enrollYear == 0) "" else s.enrollYear.toString())
        binding.edtEmail.setText(s.email)
    }

    private fun save() {
        val studentNo = binding.edtStudentNo.text?.toString()?.trim().orEmpty()
        val name = binding.edtName.text?.toString()?.trim().orEmpty()
        if (studentNo.isEmpty() || name.isEmpty()) {
            showToast("学号和姓名为必填项")
            return
        }
        val req = StudentRequest(
            studentNo = studentNo,
            name = name,
            gender = binding.edtGender.text?.toString()?.trim().orEmpty(),
            major = binding.edtMajor.text?.toString()?.trim().orEmpty(),
            className = binding.edtClassName.text?.toString()?.trim().orEmpty(),
            enrollYear = binding.edtEnrollYear.text?.toString()?.trim()?.toIntOrNull() ?: 0,
            email = binding.edtEmail.text?.toString()?.trim().orEmpty()
        )
        val request = editing?.let { viewModel.update(it.id, req) } ?: viewModel.create(req)
        request.asyncRequest().subscribe(object : CompletableObserver {
            override fun onSubscribe(d: Disposable) { disposables.add(d) }
            override fun onComplete() {
                showToast(if (editing == null) "已添加" else "已保存")
                finish()
            }
            override fun onError(e: Throwable) { showError(e.message ?: "保存失败") }
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) finish()
        return true
    }

    override fun onDestroy() {
        disposables.clear()
        super.onDestroy()
    }
}
