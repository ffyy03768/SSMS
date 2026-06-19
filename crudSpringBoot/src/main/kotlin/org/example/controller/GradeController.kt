package org.example.controller

import org.example.dto.GradeDto
import org.example.dto.GradeRequest
import org.example.service.StudentService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/** 单条成绩的修改 / 删除接口（按成绩 id 操作）。 */
@RestController
@RequestMapping("/grades")
@CrossOrigin
class GradeController(private val service: StudentService) {

    @PutMapping("/{gradeId}")
    fun update(@PathVariable gradeId: Long, @RequestBody req: GradeRequest): GradeDto =
        service.updateGrade(gradeId, req)

    @DeleteMapping("/{gradeId}")
    fun delete(@PathVariable gradeId: Long): ResponseEntity<Void> {
        service.deleteGrade(gradeId)
        return ResponseEntity.noContent().build()
    }
}
