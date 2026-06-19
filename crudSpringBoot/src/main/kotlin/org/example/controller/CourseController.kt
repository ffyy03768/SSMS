package org.example.controller

import org.example.dto.CourseDto
import org.example.dto.CourseRequest
import org.example.dto.toDto
import org.example.entity.Course
import org.example.repository.CourseRepository
import org.example.repository.GradeRepository
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException

/** 课程接口：用于前端「添加成绩」时选择课程，以及课程的增删改查管理。 */
@RestController
@RequestMapping("/courses")
@CrossOrigin
class CourseController(
    private val courseRepo: CourseRepository,
    private val gradeRepo: GradeRepository
) {

    /** 查询全部课程。 */
    @GetMapping
    fun all(): List<CourseDto> = courseRepo.findAll().map { it.toDto() }

    /** 新增课程。 */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@RequestBody req: CourseRequest): CourseDto {
        val c = Course(name = req.name, credit = req.credit, type = req.type)
        return courseRepo.save(c).toDto()
    }

    /** 修改课程。 */
    @PutMapping("/{id}")
    fun update(@PathVariable id: Long, @RequestBody req: CourseRequest): CourseDto {
        val c = courseRepo.findById(id)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "课程不存在: id=$id") }
        c.name = req.name
        c.credit = req.credit
        c.type = req.type
        return courseRepo.save(c).toDto()
    }

    /** 删除课程：若已有成绩引用该课程，则拒绝删除（返回 409）。 */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(@PathVariable id: Long) {
        if (!courseRepo.existsById(id))
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "课程不存在: id=$id")
        if (gradeRepo.existsByCourse_Id(id))
            throw ResponseStatusException(HttpStatus.CONFLICT, "该课程已有成绩记录，无法删除")
        courseRepo.deleteById(id)
    }
}
