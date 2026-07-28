package com.example.aiinterviewcoach.ui.recording

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import com.example.aiinterviewcoach.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RecordingActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(R.layout.activity_recording)
        // NavHostFragment is declared in XML layout
    }
}
