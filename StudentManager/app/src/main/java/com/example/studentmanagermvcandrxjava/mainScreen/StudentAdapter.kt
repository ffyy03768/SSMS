package com.example.studentmanagermvcandrxjava.mainScreen

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.studentmanagermvcandrxjava.databinding.ItemStudentBinding
import com.example.studentmanagermvcandrxjava.model.local.student.StudentWithGrades
import com.example.studentmanagermvcandrxjava.utils.average

/**
 * 学生列表适配器。
 * 使用 ListAdapter + DiffUtil 做差分刷新（带动画、性能好），
 * 每个 ViewHolder 持有自己的 binding（修复了原代码共用一个 binding 的回收 bug）。
 */
class StudentAdapter(
    private val onClick: (StudentWithGrades) -> Unit
) : ListAdapter<StudentWithGrades, StudentAdapter.VH>(DIFF) {

    inner class VH(private val binding: ItemStudentBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: StudentWithGrades) {
            val s = item.student
            binding.txtName.text = s.name
            binding.txtMeta.text = "${s.studentNo} · ${s.className}"
            binding.txtAvatar.text = s.name.firstOrNull()?.uppercase() ?: "?"
            binding.txtAverage.text = if (item.grades.isEmpty()) "—" else item.average().toString()
            binding.txtCourseCount.text = "${item.grades.size} 门课程"
            binding.root.setOnClickListener { onClick(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemStudentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(getItem(position))

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<StudentWithGrades>() {
            override fun areItemsTheSame(a: StudentWithGrades, b: StudentWithGrades) =
                a.student.id == b.student.id
            override fun areContentsTheSame(a: StudentWithGrades, b: StudentWithGrades) = a == b
        }
    }
}
