package org.example.entity

import jakarta.persistence.*

/**
 * 成绩表：连接「学生」与「课程」的多端。
 *
 * 每一行代表「某个学生在某门课程上的一次成绩」。
 * 这是把原来扁平的 (name, course, score) 拆开后的核心表。
 */
@Entity
@Table(name = "grade")
class Grade(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,

    @ManyToOne(optional = false)
    @JoinColumn(name = "student_id")
    var student: Student? = null,   // 外键 → student.id

    @ManyToOne(optional = false)
    @JoinColumn(name = "course_id")
    var course: Course? = null,     // 外键 → course.id

    var score: Double = 0.0,        // 成绩（用 Double，支持 85.5 这类分数）
    var term: String = ""           // 学期，例如 "2024-1"
)
