package com.example.studentmanagermvcandrxjava.detail

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.studentmanagermvcandrxjava.R
import com.example.studentmanagermvcandrxjava.databinding.ItemGradeBinding
import com.example.studentmanagermvcandrxjava.model.local.student.GradeEntity
import com.example.studentmanagermvcandrxjava.utils.ScoreBand
import com.example.studentmanagermvcandrxjava.utils.formatScore
import com.example.studentmanagermvcandrxjava.utils.scoreBand

/** 成绩列表适配器：点击编辑，长按删除；分数按区间着色。 */
class GradeAdapter(
    private val onClick: (GradeEntity) -> Unit,
    private val onLongClick: (GradeEntity) -> Unit
) : ListAdapter<GradeEntity, GradeAdapter.VH>(DIFF) {

    inner class VH(private val binding: ItemGradeBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(g: GradeEntity) {
            val ctx = binding.root.context
            binding.txtCourse.text = g.courseName
            binding.txtCredit.text = "${g.type} · ${g.credit} 学分"
            binding.txtScore.text = formatScore(g.score)
            val (bgColor, fgColor) = when (scoreBand(g.score)) {
                ScoreBand.GOOD -> R.color.scoreGoodBg to R.color.scoreGood
                ScoreBand.MID -> R.color.scoreMidBg to R.color.scoreMid
                ScoreBand.BAD -> R.color.scoreBadBg to R.color.scoreBad
            }
            binding.txtScore.backgroundTintList =
                ColorStateList.valueOf(ContextCompat.getColor(ctx, bgColor))
            binding.txtScore.setTextColor(ContextCompat.getColor(ctx, fgColor))
            binding.root.setOnClickListener { onClick(g) }
            binding.root.setOnLongClickListener { onLongClick(g); true }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemGradeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(getItem(position))

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<GradeEntity>() {
            override fun areItemsTheSame(a: GradeEntity, b: GradeEntity) = a.id == b.id
            override fun areContentsTheSame(a: GradeEntity, b: GradeEntity) = a == b
        }
    }
}
