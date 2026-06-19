package com.example.studentmanagermvcandrxjava.mainScreen

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.studentmanagermvcandrxjava.R
import com.example.studentmanagermvcandrxjava.addEdit.AddEditStudentActivity
import com.example.studentmanagermvcandrxjava.course.CourseListActivity
import com.example.studentmanagermvcandrxjava.databinding.ActivityMainBinding
import com.example.studentmanagermvcandrxjava.detail.StudentDetailActivity
import com.example.studentmanagermvcandrxjava.model.MainRepository
import com.example.studentmanagermvcandrxjava.model.local.MyDatabase
import com.example.studentmanagermvcandrxjava.model.local.student.StudentWithGrades
import com.example.studentmanagermvcandrxjava.stats.StatsActivity
import com.example.studentmanagermvcandrxjava.utils.ApiServiceSingleTon
import com.example.studentmanagermvcandrxjava.utils.AppViewModelFactory
import com.google.android.material.chip.Chip

/** 学生列表页：展示全部学生（含绩点与课程数），支持按专业筛选、搜索、点击进入详情、添加学生。 */
class MainScreenActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainScreenViewModel
    private lateinit var adapter: StudentAdapter

    private var allStudents: List<StudentWithGrades> = emptyList()
    private var query: String = ""
    private var major: String = ""   // "" 表示全部专业

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        val repository = MainRepository(
            ApiServiceSingleTon.apiService,
            MyDatabase.getDatabase(applicationContext).studentDao
        )
        viewModel = ViewModelProvider(this, AppViewModelFactory(repository))[MainScreenViewModel::class.java]

        adapter = StudentAdapter { item -> openDetail(item.student.id) }
        binding.recyclerStudents.layoutManager = LinearLayoutManager(this)
        binding.recyclerStudents.adapter = adapter

        // 专业筛选 Chip 的选择监听（单选）
        binding.chipGroupMajor.setOnCheckedChangeListener { group, checkedId ->
            val tag = if (checkedId == View.NO_ID) ""
            else group.findViewById<Chip>(checkedId)?.tag as? String ?: ""
            major = tag
            render()
        }

        viewModel.students.observe(this) {
            allStudents = it
            buildMajorChips()
            render()
        }
        viewModel.loading.observe(this) { loading ->
            binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        }
        viewModel.error.observe(this) { msg ->
            if (msg != null && allStudents.isEmpty()) {
                binding.txtEmpty.text = "加载失败：$msg"
                binding.txtEmpty.visibility = View.VISIBLE
            }
        }

        binding.btnAddStudent.setOnClickListener {
            startActivity(Intent(this, AddEditStudentActivity::class.java))
        }
    }

    /** 根据当前学生数据，重建顶部「全部 + 各专业」筛选 Chip，并保留已选项。 */
    private fun buildMajorChips() {
        val cg = binding.chipGroupMajor
        val majors = allStudents.map { it.student.major }
            .filter { it.isNotBlank() }.distinct().sorted()
        cg.removeAllViews()
        addChip(getString(R.string.filter_all), "")
        majors.forEach { addChip(it, it) }
        // 若当前选中的专业已不存在，回退到「全部」
        if (cg.checkedChipId == View.NO_ID) {
            (cg.getChildAt(0) as? Chip)?.isChecked = true
            major = ""
        }
    }

    private fun addChip(label: String, tag: String) {
        val chip = layoutInflater.inflate(R.layout.item_major_chip, binding.chipGroupMajor, false) as Chip
        chip.id = View.generateViewId()
        chip.text = label
        chip.tag = tag
        binding.chipGroupMajor.addView(chip)
        if (tag == major) chip.isChecked = true
    }

    /** 按「专业 + 关键词」过滤后刷新列表与空状态。 */
    private fun render() {
        val filtered = allStudents.filter { sg ->
            val matchMajor = major.isBlank() || sg.student.major == major
            val matchQuery = query.isBlank() ||
                sg.student.name.contains(query, ignoreCase = true) ||
                sg.student.studentNo.contains(query, ignoreCase = true)
            matchMajor && matchQuery
        }
        adapter.submitList(filtered)
        val empty = filtered.isEmpty()
        binding.recyclerStudents.visibility = if (empty) View.GONE else View.VISIBLE
        if (empty) {
            binding.txtEmpty.visibility = View.VISIBLE
            binding.txtEmpty.text = when {
                allStudents.isEmpty() -> getString(R.string.empty_students)
                else -> "没有匹配的学生"
            }
        } else {
            binding.txtEmpty.visibility = View.GONE
        }
    }

    private fun openDetail(studentId: Long) {
        val intent = Intent(this, StudentDetailActivity::class.java)
        intent.putExtra(StudentDetailActivity.EXTRA_STUDENT_ID, studentId)
        startActivity(intent)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        val searchView = menu.findItem(R.id.action_search).actionView as SearchView
        searchView.queryHint = getString(R.string.search_hint)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(text: String?): Boolean {
                query = text.orEmpty(); render(); return true
            }
            override fun onQueryTextChange(text: String?): Boolean {
                query = text.orEmpty(); render(); return true
            }
        })
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.action_courses -> {
            startActivity(Intent(this, CourseListActivity::class.java)); true
        }
        R.id.action_stats -> {
            startActivity(Intent(this, StatsActivity::class.java)); true
        }
        else -> super.onOptionsItemSelected(item)
    }
}
