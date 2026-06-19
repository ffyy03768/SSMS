package com.example.studentmanagermvcandrxjava.model.local.student

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

@Dao
abstract class StudentDao {

    /** 观察全部学生及其成绩（列表页用）。 */
    @Transaction
    @Query("SELECT * FROM student_table ORDER BY studentNo")
    abstract fun observeStudentsWithGrades(): LiveData<List<StudentWithGrades>>

    /** 观察单个学生及其成绩（详情页用）。 */
    @Transaction
    @Query("SELECT * FROM student_table WHERE id = :id")
    abstract fun observeStudentWithGrades(id: Long): LiveData<StudentWithGrades>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertStudents(students: List<StudentEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertGrades(grades: List<GradeEntity>)

    @Query("DELETE FROM student_table")
    abstract fun clearStudents()

    @Query("DELETE FROM grade_table")
    abstract fun clearGrades()

    /** 用服务器返回的最新数据整体替换本地缓存（在一个事务里完成）。 */
    @Transaction
    open fun replaceAll(students: List<StudentEntity>, grades: List<GradeEntity>) {
        clearGrades()
        clearStudents()
        insertStudents(students)
        insertGrades(grades)
    }
}
