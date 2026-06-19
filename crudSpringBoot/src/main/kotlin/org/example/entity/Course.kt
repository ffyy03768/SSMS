package org.example.entity

import jakarta.persistence.*

/** 课程表：课程基本信息。一门课程可以被多条成绩引用。 */
@Entity
@Table(name = "course")
class Course(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,

    @Column(nullable = false)
    var name: String = "",     // 课程名
    var credit: Int = 0,       // 学分
    var type: String = "必修"  // 必修 / 选修
)
