package com.example.aiinterviewcoach.ui.resume

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.aiinterviewcoach.databinding.ItemResumeRecommendationBinding
import com.example.aiinterviewcoach.scoring.RecommendationItem

class ResumeRecommendationAdapter(
    private var recommendations: List<RecommendationItem> = emptyList(),
    private val onItemClick: (RecommendationItem) -> Unit
) : RecyclerView.Adapter<ResumeRecommendationAdapter.RecViewHolder>() {

    fun submitList(newList: List<RecommendationItem>) {
        recommendations = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecViewHolder {
        val binding = ItemResumeRecommendationBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return RecViewHolder(binding, onItemClick)
    }

    override fun onBindViewHolder(holder: RecViewHolder, position: Int) {
        holder.bind(recommendations[position])
    }

    override fun getItemCount(): Int = recommendations.size

    class RecViewHolder(
        private val binding: ItemResumeRecommendationBinding,
        private val onItemClick: (RecommendationItem) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: RecommendationItem) {
            binding.tvRecTitle.text = item.title
            binding.tvRecDescription.text = item.description
            binding.root.setOnClickListener {
                onItemClick(item)
            }
        }
    }
}
