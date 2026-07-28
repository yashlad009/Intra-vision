package com.example.aiinterviewcoach.ui.recording

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.MediaController
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.aiinterviewcoach.databinding.FragmentPlaybackBinding
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import java.io.File

@AndroidEntryPoint
class PlaybackFragment : Fragment() {

    private var _binding: FragmentPlaybackBinding? = null
    private val binding get() = _binding!!

    private val args: PlaybackFragmentArgs by navArgs()
    private var mediaController: MediaController? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlaybackBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        val filename = args.filename
        binding.tvTitle.text = filename.take(100)

        // Resolve file path in internal storage recordings directory
        val file = File(File(requireContext().filesDir, "recordings"), filename)
        if (file.exists()) {
            mediaController = MediaController(requireActivity()).apply {
                setAnchorView(binding.videoView)
            }
            binding.videoView.apply {
                setMediaController(mediaController)
                setVideoPath(file.absolutePath)
                setOnPreparedListener { mediaPlayer ->
                    mediaPlayer.start()
                }
                setOnErrorListener { _, what, extra ->
                    Snackbar.make(binding.root, "Playback failed ($what, $extra)", Snackbar.LENGTH_LONG).show()
                    true
                }
            }
        } else {
            Snackbar.make(binding.root, "File not found: $filename", Snackbar.LENGTH_LONG).show()
        }
    }

    override fun onStop() {
        super.onStop()
        binding.videoView.stopPlayback()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        mediaController = null
    }
}
