package org.example.student_spring

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

/**
 * 应用入口。
 *
 * 注意：本类位于 org.example.student_spring 包，而改造后的新代码位于 org.example 下的
 * config / controller / dto / entity / repository / service 子包（与本包平级）。
 * Spring Boot 默认只扫描入口类所在包及其子包，因此这里显式扩大扫描范围，否则新代码不会被加载：
 *   - scanBasePackages：组件（@RestController / @Service / @Component 等）扫描根包
 *   - @EntityScan：JPA 实体所在包
 *   - @EnableJpaRepositories：Spring Data 仓库接口所在包
 */
@SpringBootApplication(scanBasePackages = ["org.example"])
@EntityScan(basePackages = ["org.example.entity"])
@EnableJpaRepositories(basePackages = ["org.example.repository"])
class StudentSpringApplication

fun main(args: Array<String>) {
    runApplication<StudentSpringApplication>(*args)
}
