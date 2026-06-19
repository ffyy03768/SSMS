package com.example.studentmanagermvcandrxjava.model.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.studentmanagermvcandrxjava.model.local.student.GradeEntity
import com.example.studentmanagermvcandrxjava.model.local.student.StudentDao
import com.example.studentmanagermvcandrxjava.model.local.student.StudentEntity

@Database(
    entities = [StudentEntity::class, GradeEntity::class],
    version = 2,
    exportSchema = false
)
abstract class MyDatabase : RoomDatabase() {

    abstract val studentDao: StudentDao

    companion object {
        @Volatile
        private var database: MyDatabase? = null

        fun getDatabase(context: Context): MyDatabase =
            database ?: synchronized(this) {
                database ?: Room.databaseBuilder(
                    context.applicationContext,
                    MyDatabase::class.java,
                    "studentManager.db"
                ).fallbackToDestructiveMigration().build().also { database = it }
            }
    }
}
