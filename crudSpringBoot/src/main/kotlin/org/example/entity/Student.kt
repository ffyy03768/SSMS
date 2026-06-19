package org.example.entity

import jakarta.persistence.*

/**
 * 学生主表。
 *
 * 一个学生（1）可以对应多条成绩记录（N），即「一对多」关系。
 * 删除学生时，通过 cascade + orphanRemoval 级联删除其全部成绩。
 */
@Entity
@Table(name = "student")
class Student(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,

    @Column(nullable = false)
    var studentNo: String = "",   // 学号

    @Column(nullable = false)
    var name: String = "",        // 姓名

    var gender: String = "",      // 性别
    var major: String = "",       // 专业
    var className: String = "",   // 班级
    var enrollYear: Int = 0,      // 入学年份
    var email: String = "",       // 邮箱

    // 一对多：mappedBy 指向 Grade.student
    @OneToMany(mappedBy = "student", cascade = [CascadeType.ALL], orphanRemoval = true)
    var grades: MutableList<Grade> = mutableListOf()
)
