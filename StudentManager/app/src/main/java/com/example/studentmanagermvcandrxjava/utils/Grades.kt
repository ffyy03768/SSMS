package com.example.studentmanagermvcandrxjava.utils

import com.example.studentmanagermvcandrxjava.model.local.student.StudentWithGrades

/** 平均分（保留 1 位小数）。 */
fun StudentWithGrades.average(): Double =
    if (grades.isEmpty()) 0.0
    else Math.round(grades.map { it.score }.average() * 10) / 10.0

/**
 * 学分加权绩点：先算学分加权平均分，再按 (加权平均分 − 50) / 10 换算（保留 2 位小数）。
 * 例：加权平均分 90 → 绩点 4.0；80 → 3.0；100 → 5.0。
 */
fun StudentWithGrades.gpa(): Double {
    if (grades.isEmpty()) return 0.0
    val totalCredit = grades.sumOf { it.credit }
    if (totalCredit == 0) return 0.0
    val weightedAvg = grades.sumOf { it.score * it.credit } / totalCredit
    val point = (weightedAvg - 50) / 10.0
    return Math.round(point * 100) / 100.0
}

/** 成绩等级，用于给分数着色。 */
enum class ScoreBand { GOOD, MID, BAD }

fun scoreBand(score: Double): ScoreBand = when {
    score >= 85 -> ScoreBand.GOOD
    score >= 60 -> ScoreBand.MID
    else -> ScoreBand.BAD
}

/** 整数分数显示为整数（95 而不是 95.0）。 */
fun formatScore(score: Double): String =
    if (score % 1.0 == 0.0) score.toInt().toString() else score.toString()
