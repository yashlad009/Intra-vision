package com.example.aiinterviewcoach.ui.aptitude

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.RecyclerView
import com.example.aiinterviewcoach.R
import io.noties.markwon.Markwon

class AptitudeStudyPagerAdapter(
    private val learnSections: List<MarkdownSection>,
    private val practiceSections: List<MarkdownSection>,
    private val reviseSections: List<MarkdownSection>,
    private val markwon: Markwon
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun getItemCount(): Int = 3

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            0 -> {
                val view = inflater.inflate(R.layout.tab_study_learn, parent, false)
                LearnViewHolder(view)
            }
            else -> {
                val view = inflater.inflate(R.layout.tab_study_scrollable, parent, false)
                ScrollableViewHolder(view)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (position) {
            0 -> (holder as LearnViewHolder).bind(learnSections, markwon)
            1 -> (holder as ScrollableViewHolder).bind(practiceSections, markwon)
            2 -> (holder as ScrollableViewHolder).bind(reviseSections, markwon)
        }
    }

    class LearnViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val chipsContainer = itemView.findViewById<LinearLayout>(R.id.llChipsContainer)
        private val contentContainer = itemView.findViewById<LinearLayout>(R.id.llLearnContentContainer)
        private val scrollView = itemView.findViewById<NestedScrollView>(R.id.nsvLearnContent)

        fun bind(sections: List<MarkdownSection>, markwon: Markwon) {
            contentContainer.removeAllViews()
            chipsContainer.removeAllViews()

            val density = itemView.resources.displayMetrics.density
            val dpToPx = { dp: Int -> (dp * density).toInt() }

            val viewMap = mutableMapOf<String, View>()

            for (section in sections) {
                val sectionView = LinearLayout(itemView.context).apply {
                    orientation = LinearLayout.VERTICAL
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        setMargins(0, 0, 0, dpToPx(24))
                    }
                }

                val titleTv = TextView(itemView.context).apply {
                    text = section.title
                    textSize = 20f
                    setTextColor(Color.parseColor("#0F6E56"))
                    paint.isFakeBoldText = true
                    setPadding(0, 0, 0, dpToPx(8))
                }
                sectionView.addView(titleTv)

                val bodyTv = TextView(itemView.context).apply {
                    textSize = 15f
                    setTextColor(Color.parseColor("#0F172A"))
                    setLineSpacing(0f, 1.2f)
                }
                markwon.setMarkdown(bodyTv, section.content)
                sectionView.addView(bodyTv)

                contentContainer.addView(sectionView)
                viewMap[section.title] = sectionView

                // Add to chips row
                val chip = TextView(itemView.context).apply {
                    text = section.title
                    setTextColor(Color.parseColor("#0F6E56"))
                    setBackgroundResource(R.drawable.bg_chip_outline)
                    setPadding(dpToPx(16), dpToPx(8), dpToPx(16), dpToPx(8))
                    val params = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        setMargins(dpToPx(4), 0, dpToPx(4), 0)
                    }
                    layoutParams = params
                    setOnClickListener {
                        viewMap[section.title]?.let { targetView ->
                            scrollView.post {
                                scrollView.smoothScrollTo(0, targetView.top)
                            }
                        }
                    }
                }
                chipsContainer.addView(chip)
            }
        }
    }

    class ScrollableViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val contentContainer = itemView.findViewById<LinearLayout>(R.id.llScrollContentContainer)

        fun bind(sections: List<MarkdownSection>, markwon: Markwon) {
            contentContainer.removeAllViews()

            val density = itemView.resources.displayMetrics.density
            val dpToPx = { dp: Int -> (dp * density).toInt() }

            if (sections.isEmpty()) {
                val emptyTv = TextView(itemView.context).apply {
                    text = "No additional content in this section."
                    textSize = 15f
                    setTextColor(Color.parseColor("#64748B"))
                }
                contentContainer.addView(emptyTv)
                return
            }

            for (section in sections) {
                val sectionView = LinearLayout(itemView.context).apply {
                    orientation = LinearLayout.VERTICAL
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        setMargins(0, 0, 0, dpToPx(24))
                    }
                }

                val titleTv = TextView(itemView.context).apply {
                    text = section.title
                    textSize = 20f
                    setTextColor(Color.parseColor("#0F6E56"))
                    paint.isFakeBoldText = true
                    setPadding(0, 0, 0, dpToPx(8))
                }
                sectionView.addView(titleTv)

                val bodyTv = TextView(itemView.context).apply {
                    textSize = 15f
                    setTextColor(Color.parseColor("#0F172A"))
                    setLineSpacing(0f, 1.2f)
                }
                markwon.setMarkdown(bodyTv, section.content)
                sectionView.addView(bodyTv)

                contentContainer.addView(sectionView)
            }
        }
    }
}
