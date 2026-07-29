package com.example.aiinterviewcoach.ui.resume

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.aiinterviewcoach.R
import com.example.aiinterviewcoach.databinding.ItemResumeCheckBinding
import com.example.aiinterviewcoach.scoring.CheckResult

class ResumeCheckAdapter(
    private var checks: List<CheckResult> = emptyList()
) : RecyclerView.Adapter<ResumeCheckAdapter.CheckViewHolder>() {

    fun submitList(newList: List<CheckResult>) {
        checks = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CheckViewHolder {
        val binding = ItemResumeCheckBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CheckViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CheckViewHolder, position: Int) {
        holder.bind(checks[position])
    }

    override fun getItemCount(): Int = checks.size

    class CheckViewHolder(private val binding: ItemResumeCheckBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(check: CheckResult) {
            val ctx = binding.root.context
            binding.tvCheckTitle.text = check.title
            binding.tvCheckExplanation.text = check.explanation

            if (check.isPassed) {
                binding.ivCheckStatus.setImageResource(R.drawable.ic_check_circle)
                binding.tvStatusBadge.text = "PASSED"
                binding.tvStatusBadge.setTextColor(Color.parseColor("#0F6E56"))
                binding.tvStatusBadge.setBackgroundColor(Color.parseColor("#E1F5EE"))
            } else {
                binding.ivCheckStatus.setImageResource(R.drawable.ic_warning)
                binding.tvStatusBadge.text = "FLAGGED"
                binding.tvStatusBadge.setTextColor(Color.parseColor("#D97706"))
                binding.tvStatusBadge.setBackgroundColor(Color.parseColor("#FEF3C7"))
            }
        }
    }
}
