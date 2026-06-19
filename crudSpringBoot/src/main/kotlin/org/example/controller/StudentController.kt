package org.example.controller

import org.example.dto.GradeDto
import org.example.dto.GradeRequest
import org.example.dto.StudentDto
import org.example.dto.StudentRequest
import org.example.service.StudentService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * 学生相关 REST 接口。
 * @CrossOrigin 允许浏览器 / 模拟器跨域访问（方便联调）。
 */
@RestController
@RequestMapping("/students")
@CrossOrigin
class StudentController(private val service: StudentService) {

    @GetMapping
    fun all(): List<StudentDto> = service.findAll()

    @GetMapping("/{id}")
    fun one(@PathVariable id: Long): StudentDto = service.findOne(id)

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@RequestBody req: StudentRequest): StudentDto = service.create(req)

    @PutMapping("/{id}")
    fun update(@PathVariable id: Long, @RequestBody req: StudentRequest): StudentDto =
        service.update(id, req)

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long): ResponseEntity<Void> {
        service.delete(id)
        return ResponseEntity.noContent().build()
    }

    /** 给指定学生新增一条成绩。 */
    @PostMapping("/{id}/grades")
    @ResponseStatus(HttpStatus.CREATED)
    fun addGrade(@PathVariable id: Long, @RequestBody req: GradeRequest): GradeDto =
        service.addGrade(id, req)
}
