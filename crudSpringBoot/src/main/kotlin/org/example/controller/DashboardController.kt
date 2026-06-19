package org.example.controller

import org.example.service.StudentService
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping

/**
 * 数据库可视化看板（Thymeleaf 服务端渲染页面）。
 * 浏览器访问 http://localhost:8080/dashboard 即可看到美观的学生成绩总览，
 * 适合直接截图放进实验报告，替代朴素的 H2 控制台截图。
 */
@Controller
class DashboardController(private val service: StudentService) {

    @GetMapping("/")
    fun home(): String = "redirect:/dashboard"

    @GetMapping("/dashboard")
    fun dashboard(model: Model): String {
        val students = service.findAll()
        val allGrades = students.flatMap { it.grades }
        model.addAttribute("students", students)
        model.addAttribute("studentCount", students.size)
        model.addAttribute("gradeCount", allGrades.size)
        model.addAttribute("courseCount", allGrades.map { it.courseId }.distinct().size)
        model.addAttribute(
            "overallAvg",
            if (allGrades.isEmpty()) "0.0" else String.format("%.1f", allGrades.map { it.score }.average())
        )
        // 每门课程在所有学生中的平均分（从高到低）
        val courseAverages = allGrades.groupBy { it.courseName }
            .map { (name, gs) ->
                val a = gs.map { it.score }.average()
                val band = if (a >= 85) "good" else if (a >= 60) "mid" else "bad"
                CourseAvg(name, String.format("%.1f", a), gs.size, band)
            }
            .sortedByDescending { it.avg.toDouble() }
        model.addAttribute("courseAverages", courseAverages)
        return "dashboard"
    }
}

/** 课程平均分（看板用）。 */
data class CourseAvg(val name: String, val avg: String, val count: Int, val band: String)
