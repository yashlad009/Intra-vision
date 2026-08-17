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

        private fun parseMetadata(content: String): Map<String, String>? {
            val lines = content.split("\n")
            if (lines.none { it.contains("Module:", ignoreCase = true) }) return null

            val metadata = mutableMapOf<String, String>()
            var currentKey: String? = null
            val currentValue = java.lang.StringBuilder()

            for (line in lines) {
                val cleanLine = line.trim().removePrefix(">").trim()
                if (cleanLine.isEmpty() || cleanLine == "---") continue

                val colonIndex = cleanLine.indexOf(":")
                if (colonIndex != -1) {
                    val potentialKey = cleanLine.substring(0, colonIndex).trim()
                    val lowerKey = potentialKey.lowercase()
                    if (lowerKey == "module" || lowerKey == "difficulty" || lowerKey == "estimated learning time" || lowerKey == "prerequisite" || lowerKey == "prerequisites") {
                        if (currentKey != null) {
                            metadata[currentKey] = currentValue.toString().trim()
                            currentValue.setLength(0)
                        }
                        currentKey = potentialKey
                        val valPart = cleanLine.substring(colonIndex + 1).trim()
                        if (valPart.isNotEmpty()) {
                            currentValue.append(valPart)
                        }
                        continue
                    }
                }

                if (currentKey != null) {
                    val cleanItem = cleanLine.removePrefix("-").trim()
                    if (cleanItem.isNotEmpty()) {
                        if (currentValue.isNotEmpty()) {
                            currentValue.append(", ").append(cleanItem)
                        } else {
                            currentValue.append(cleanItem)
                        }
                    }
                }
            }
            if (currentKey != null && currentValue.isNotEmpty()) {
                metadata[currentKey] = currentValue.toString().trim()
            }
            return if (metadata.isEmpty()) null else metadata
        }

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

                val metadataMap = parseMetadata(section.content)
                if (metadataMap != null) {
                    val cardLayout = LinearLayout(itemView.context).apply {
                        orientation = LinearLayout.VERTICAL
                        setPadding(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(16))
                        val cardBackground = android.graphics.drawable.GradientDrawable().apply {
                            setColor(Color.parseColor("#F0FDF4")) // Soft green tint
                            cornerRadius = dpToPx(12).toFloat()
                            setStroke(dpToPx(1), Color.parseColor("#CCFBF1")) // soft green border
                        }
                        background = cardBackground
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        ).apply {
                            setMargins(0, 0, 0, dpToPx(16))
                        }
                    }

                    for ((key, value) in metadataMap) {
                        val rowLayout = LinearLayout(itemView.context).apply {
                            orientation = LinearLayout.HORIZONTAL
                            layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                            ).apply {
                                setMargins(0, 0, 0, dpToPx(8))
                            }
                        }

                        val labelTv = TextView(itemView.context).apply {
                            text = "$key: "
                            textSize = 14f
                            setTextColor(Color.parseColor("#475569"))
                            paint.isFakeBoldText = true
                            layoutParams = LinearLayout.LayoutParams(
                                dpToPx(120),
                                LinearLayout.LayoutParams.WRAP_CONTENT
                            )
                        }

                        val valueTv = TextView(itemView.context).apply {
                            text = value
                            textSize = 14f
                            setTextColor(Color.parseColor("#0F172A"))
                            layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                            )
                        }

                        rowLayout.addView(labelTv)
                        rowLayout.addView(valueTv)
                        cardLayout.addView(rowLayout)
                    }
                    sectionView.addView(cardLayout)
                } else {
                    val bodyTv = TextView(itemView.context).apply {
                        textSize = 15f
                        setTextColor(Color.parseColor("#0F172A"))
                        setLineSpacing(0f, 1.2f)
                    }
                    markwon.setMarkdown(bodyTv, section.content)
                    sectionView.addView(bodyTv)
                }

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
                                val rect = android.graphics.Rect()
                                targetView.getDrawingRect(rect)
                                try {
                                    scrollView.offsetDescendantRectToMyCoords(targetView, rect)
                                    scrollView.smoothScrollTo(0, rect.top)
                                } catch (e: IllegalArgumentException) {
                                    // Fallback if not a direct descendant layout wise
                                    scrollView.smoothScrollTo(0, targetView.top)
                                }
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
