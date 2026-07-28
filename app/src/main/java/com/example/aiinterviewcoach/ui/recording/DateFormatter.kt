package com.example.aiinterviewcoach.ui.recording

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun formatDate(createdAt: Long): String {
    if (createdAt <= 0) return "Unknown date"
    return SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date(createdAt))
}
