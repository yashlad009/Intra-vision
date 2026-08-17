package com.example.aiinterviewcoach.ui.aptitude

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.aiinterviewcoach.databinding.FragmentStudyChatBinding
import com.example.aiinterviewcoach.databinding.ItemChatMessageBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class StudyChatFragment : Fragment() {

    private val viewModel: StudyChatViewModel by viewModels()
    private val args: StudyChatFragmentArgs by navArgs()
    private var _binding: FragmentStudyChatBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStudyChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val category = args.category
        val topicId = args.topicId
        val topicName = args.topicName

        binding.toolbarChat.title = "Ask AI: $topicName"
        binding.toolbarChat.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        viewModel.initTopic(category, topicId)

        val chatAdapter = StudyChatAdapter()
        binding.rvChatHistory.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = chatAdapter
        }

        binding.btnSend.setOnClickListener {
            val txt = binding.etChatMessage.text.toString().trim()
            if (txt.isNotEmpty()) {
                viewModel.sendMessage(txt)
                binding.etChatMessage.setText("")
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.state.collect { state ->
                chatAdapter.submitList(state.messages)
                
                binding.pbChatLoading.visibility = if (state.isLoading) View.VISIBLE else View.GONE
                binding.btnSend.isEnabled = !state.isLoading

                if (state.messages.isNotEmpty()) {
                    binding.rvChatHistory.post {
                        binding.rvChatHistory.smoothScrollToPosition(state.messages.lastIndex)
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

class StudyChatAdapter : RecyclerView.Adapter<StudyChatAdapter.ViewHolder>() {

    private var list: List<ChatMessage> = emptyList()

    fun submitList(newList: List<ChatMessage>) {
        list = newList
        notifyDataSetChanged()
    }

    class ViewHolder(val binding: ItemChatMessageBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemChatMessageBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        if (item.isUser) {
            holder.binding.llUserBubble.visibility = View.VISIBLE
            holder.binding.llAiBubble.visibility = View.GONE
            holder.binding.tvUserMsg.text = item.text
        } else {
            holder.binding.llUserBubble.visibility = View.GONE
            holder.binding.llAiBubble.visibility = View.VISIBLE
            holder.binding.tvAiMsg.text = item.text
        }
    }

    override fun getItemCount(): Int = list.size
}
