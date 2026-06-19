package com.example.studentmanagermvcandrxjava.stats

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.studentmanagermvcandrxjava.R
import com.example.studentmanagermvcandrxjava.databinding.ActivityStatsBinding
import com.example.studentmanagermvcandrxjava.model.MainRepository
import com.example.studentmanagermvcandrxjava.model.local.MyDatabase
import com.example.studentmanagermvcandrxjava.model.local.student.StudentWithGrades
import com.example.studentmanagermvcandrxjava.utils.ApiServiceSingleTon
import com.example.studentmanagermvcandrxjava.utils.AppViewModelFactory
import com.example.studentmanagermvcandrxjava.utils.ScoreBand
import com.example.studentmanagermvcandrxjava.utils.average
import com.example.studentmanagermvcandrxjava.utils.gpa
import com.example.studentmanagermvcandrxjava.utils.scoreBand

/** 统计 / 排名页：总体概况、成绩分布、各门课平均分，以及按绩点排序的学生排名。 */
class StatsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStatsBinding
    private lateinit var viewModel: StatsViewModel
    private lateinit var adapter: RankAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStatsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        val repository = MainRepository(
            ApiServiceSingleTon.apiService,
            MyDatabase.getDatabase(applicationContext).studentDao
        )
        viewModel = ViewModelProvider(this, AppViewModelFactory(repository))[StatsViewModel::class.java]

        adapter = RankAdapter()
        binding.recyclerRank.layoutManager = LinearLayoutManager(this)
        binding.recyclerRank.adapter = adapter

        viewModel.students.observe(this) { list ->
            renderOverall(list)
            renderCourseAverages(list)
            renderRanking(list)
        }
        viewModel.courseCount.observe(this) { binding.txtCourseCount.text = it.toString() }
    }

    private fun renderOverall(list: List<StudentWithGrades>) {
        binding.txtStudentCount.text = list.size.toString()
        val allGrades = list.flatMap { it.grades }
        binding.txtGradeCount.text = allGrades.size.toString()
        binding.txtOverallAvg.text =
            if (allGrades.isEmpty()) "—"
            else (Math.round(allGrades.map { it.score }.average() * 10) / 10.0).toString()

        binding.txtGood.text = allGrades.count { scoreBand(it.score) == ScoreBand.GOOD }.toString()
        binding.txtMid.text = allGrades.count { scoreBand(it.score) == ScoreBand.MID }.toString()
        binding.txtBad.text = allGrades.count { scoreBand(it.score) == ScoreBand.BAD }.toString()
    }

    /** 计算每门课程在所有学生中的平均分，从高到低列出。 */
    private fun renderCourseAverages(list: List<StudentWithGrades>) {
        val container = binding.courseAvgContainer
        container.removeAllViews()
        val byCourse = list.flatMap { it.grades }.groupBy { it.courseName }
        if (byCourse.isEmpty()) {
            binding.cardCourseAvg.visibility = View.GONE
            return
        }
        binding.cardCourseAvg.visibility = View.VISIBLE
        byCourse.map { (name, grades) -> Triple(name, grades.map { it.score }.average(), grades.size) }
            .sortedByDescending { it.second }
            .forEach { (name, avg, count) ->
                val row = layoutInflater.inflate(R.layout.item_course_avg, container, false)
                row.findViewById<TextView>(R.id.txtCourseAvgName).text = name
                row.findViewById<TextView>(R.id.txtCourseAvgCount).text = "$count 人"
                val valueView = row.findViewById<TextView>(R.id.txtCourseAvgValue)
                valueView.text = (Math.round(avg * 10) / 10.0).toString()
                valueView.setTextColor(ContextCompat.getColor(this, bandColor(avg)))
                container.addView(row)
            }
    }

    private fun renderRanking(list: List<StudentWithGrades>) {
        // 有成绩的按绩点从高到低；没有成绩的排在最后
        val ranked = list.sortedByDescending { if (it.grades.isEmpty()) -1.0 else it.gpa() }
        val rows = ranked.mapIndexed { index, sg ->
            val meta = listOf(sg.student.major, sg.student.className)
                .filter { it.isNotBlank() }.joinToString(" · ")
            RankRow(
                rank = index + 1,
                name = sg.student.name,
                meta = meta,
                gpa = if (sg.grades.isEmpty()) "—" else sg.gpa().toString(),
                average = if (sg.grades.isEmpty()) "—" else sg.average().toString()
            )
        }
        adapter.submitList(rows)
        val empty = rows.isEmpty()
        binding.recyclerRank.visibility = if (empty) View.GONE else View.VISIBLE
        binding.txtRankEmpty.visibility = if (empty) View.VISIBLE else View.GONE
    }

    private fun bandColor(score: Double): Int = when (scoreBand(score)) {
        ScoreBand.GOOD -> R.color.scoreGood
        ScoreBand.MID -> R.color.scoreMid
        ScoreBand.BAD -> R.color.scoreBad
    }
}
