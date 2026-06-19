package com.example.studentmanagermvcandrxjava.course

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.studentmanagermvcandrxjava.databinding.ItemCourseBinding
import com.example.studentmanagermvcandrxjava.model.api.CourseDto

/** 课程列表适配器：点击编辑，长按删除。 */
class CourseAdapter(
    private val onClick: (CourseDto) -> Unit,
    private val onLongClick: (CourseDto) -> Unit
) : ListAdapter<CourseDto, CourseAdapter.VH>(DIFF) {

    inner class VH(private val binding: ItemCourseBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: CourseDto) {
            binding.txtCredit.text = item.credit.toString()
            binding.txtCourseName.text = item.name
            binding.txtCourseType.text = "${item.type} · ${item.credit} 学分"
            binding.root.setOnClickListener { onClick(item) }
            binding.root.setOnLongClickListener { onLongClick(item); true }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemCourseBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(getItem(position))

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<CourseDto>() {
            override fun areItemsTheSame(a: CourseDto, b: CourseDto) = a.id == b.id
            override fun areContentsTheSame(a: CourseDto, b: CourseDto) = a == b
        }
    }
}
