package org.example.config

import org.example.entity.Course
import org.example.entity.Grade
import org.example.entity.Student
import org.example.repository.CourseRepository
import org.example.repository.GradeRepository
import org.example.repository.StudentRepository
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

/**
 * 启动时写入示例数据（仅当库为空时执行），
 * 这样第一次运行就能在 App 和看板里看到内容，方便截图与演示。
 */
@Component
class DataSeeder(
    private val studentRepo: StudentRepository,
    private val courseRepo: CourseRepository,
    private val gradeRepo: GradeRepository
) : CommandLineRunner {

    @Transactional
    override fun run(vararg args: String?) {
        if (studentRepo.count() > 0L) return  // 已有数据则跳过

        val courses = courseRepo.saveAll(
            listOf(
                Course(name = "高等数学", credit = 4, type = "必修"),
                Course(name = "数据结构", credit = 4, type = "必修"),
                Course(name = "计算机网络", credit = 3, type = "必修"),
                Course(name = "大学英语", credit = 3, type = "必修"),
                Course(name = "操作系统", credit = 4, type = "必修"),
                Course(name = "大学体育", credit = 1, type = "必修")
            )
        ).toList()

        // (学号, 姓名, 性别, 专业, 班级, 各门成绩)
        data class Seed(
            val no: String, val name: String, val gender: String,
            val major: String, val cls: String, val scores: List<Double>
        )

        val people = listOf(
            Seed("2023012045", "林若曦", "女", "计算机科学与技术", "计算机2301", listOf(95.0, 92.0, 90.0, 88.0, 86.0, 82.0)),
            Seed("2023012018", "陈思远", "男", "计算机科学与技术", "计算机2301", listOf(88.0, 79.0, 85.0, 90.0, 82.0, 83.0)),
            Seed("2023011902", "王梓萱", "女", "软件工程", "软件2302", listOf(91.0, 87.0, 84.0, 93.0, 80.0)),
            Seed("2023012077", "赵宇航", "男", "计算机科学与技术", "计算机2301", listOf(76.0, 82.0, 78.0, 85.0, 74.0, 81.0))
        )

        people.forEach { p ->
            val s = studentRepo.save(
                Student(
                    studentNo = p.no, name = p.name, gender = p.gender,
                    major = p.major, className = p.cls, enrollYear = 2023,
                    email = "${p.no}@stu.edu.cn"
                )
            )
            p.scores.forEachIndexed { i, sc ->
                gradeRepo.save(Grade(student = s, course = courses[i], score = sc, term = "2024-1"))
            }
        }
    }
}
