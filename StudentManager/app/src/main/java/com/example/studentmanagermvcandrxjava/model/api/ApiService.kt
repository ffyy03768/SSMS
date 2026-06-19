package com.example.studentmanagermvcandrxjava.model.api

import io.reactivex.Completable
import io.reactivex.Single
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

/** Retrofit 接口：与后端学生 / 成绩 / 课程接口一一对应。 */
interface ApiService {

    @GET("students")
    fun getAllStudents(): Single<List<StudentDto>>

    @GET("students/{id}")
    fun getStudent(@Path("id") id: Long): Single<StudentDto>

    @POST("students")
    fun createStudent(@Body body: StudentRequest): Single<StudentDto>

    @PUT("students/{id}")
    fun updateStudent(@Path("id") id: Long, @Body body: StudentRequest): Single<StudentDto>

    @DELETE("students/{id}")
    fun deleteStudent(@Path("id") id: Long): Completable

    @POST("students/{id}/grades")
    fun addGrade(@Path("id") id: Long, @Body body: GradeRequest): Single<GradeDto>

    @PUT("grades/{id}")
    fun updateGrade(@Path("id") id: Long, @Body body: GradeRequest): Single<GradeDto>

    @DELETE("grades/{id}")
    fun deleteGrade(@Path("id") id: Long): Completable

    @GET("courses")
    fun getCourses(): Single<List<CourseDto>>

    @POST("courses")
    fun createCourse(@Body body: CourseRequest): Single<CourseDto>

    @PUT("courses/{id}")
    fun updateCourse(@Path("id") id: Long, @Body body: CourseRequest): Single<CourseDto>

    @DELETE("courses/{id}")
    fun deleteCourse(@Path("id") id: Long): Completable
}
