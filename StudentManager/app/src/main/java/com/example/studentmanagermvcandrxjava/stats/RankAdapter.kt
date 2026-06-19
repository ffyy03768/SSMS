package com.example.studentmanagermvcandrxjava.stats

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.studentmanagermvcandrxjava.databinding.ItemRankBinding

/** 排名行数据：综合成绩用绩点（gpa）作为主指标，平均分（average）作为辅助显示。 */
data class RankRow(
    val rank: Int,
    val name: String,
    val meta: String,
    val gpa: String,
    val average: String
)

/** 成绩排名适配器（按绩点排序）。 */
class RankAdapter : ListAdapter<RankRow, RankAdapter.VH>(DIFF) {

    inner class VH(private val binding: ItemRankBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: RankRow) {
            binding.txtRank.text = item.rank.toString()
            binding.txtRankName.text = item.name
            binding.txtRankMeta.text = item.meta
            binding.txtRankAverage.text = item.gpa            // 主：绩点
            binding.txtRankGpa.text = "平均分 ${item.average}" // 辅：平均分
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemRankBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(getItem(position))

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<RankRow>() {
            override fun areItemsTheSame(a: RankRow, b: RankRow) = a.name == b.name && a.rank == b.rank
            override fun areContentsTheSame(a: RankRow, b: RankRow) = a == b
        }
    }
}
