package com.example.aiinterviewcoach.ui.recording

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View

class OverlayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paintJoint = Paint().apply {
        color = Color.GREEN
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    private val paintBone = Paint().apply {
        color = Color.parseColor("#00E5FF") // Neon Cyan
        style = Paint.Style.STROKE
        strokeWidth = 8f
        isAntiAlias = true
    }

    private val paintFaceBox = Paint().apply {
        color = Color.parseColor("#8B5CF6") // Accent Purple
        style = Paint.Style.STROKE
        strokeWidth = 6f
        isAntiAlias = true
    }

    private val paintStatusBg = Paint().apply {
        color = Color.parseColor("#CC0F172A") // Semi-transparent Slate-900
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    private val paintTextGood = Paint().apply {
        color = Color.parseColor("#10B981") // Success Emerald
        textSize = 44f
        style = Paint.Style.FILL
        isAntiAlias = true
        isFakeBoldText = true
    }

    private val paintTextWarning = Paint().apply {
        color = Color.parseColor("#EF4444") // Danger Red
        textSize = 44f
        style = Paint.Style.FILL
        isAntiAlias = true
        isFakeBoldText = true
    }

    private val paintTextTitle = Paint().apply {
        color = Color.WHITE
        textSize = 36f
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    private var faceBox: RectF? = null
    private var eyeContactMaintained: Boolean? = null
    private var postureFeedback: String? = null
    private var poseLandmarks: List<PointF> = emptyList()
    private val connections = listOf(
        Pair(0, 1),   // Left Shoulder to Right Shoulder
        Pair(0, 2),   // Left Shoulder to Left Elbow
        Pair(2, 4),   // Left Elbow to Left Wrist
        Pair(1, 3),   // Right Shoulder to Right Elbow
        Pair(3, 5),   // Right Elbow to Right Wrist
        Pair(0, 6),   // Left Shoulder to Left Hip
        Pair(1, 7),   // Right Shoulder to Right Hip
        Pair(6, 7)    // Left Hip to Right Hip
    )

    fun updateOverlay(
        rawFaceBox: android.graphics.Rect?,
        eyeContact: Boolean?,
        postureFeedback: String?,
        rawLandmarks: List<PointF>,
        imageWidth: Int,
        imageHeight: Int,
        rotationDegrees: Int
    ) {
        this.eyeContactMaintained = eyeContact
        this.postureFeedback = postureFeedback

        // Map Pose landmarks to OverlayView coordinates
        this.poseLandmarks = rawLandmarks.map { pt ->
            translate(pt.x, pt.y, imageWidth, imageHeight)
        }

        // Map Face box to OverlayView coordinates
        rawFaceBox?.let { box ->
            val p1 = translate(box.left.toFloat(), box.top.toFloat(), imageWidth, imageHeight)
            val p2 = translate(box.right.toFloat(), box.bottom.toFloat(), imageWidth, imageHeight)
            
            val left = minOf(p1.x, p2.x)
            val right = maxOf(p1.x, p2.x)
            val top = minOf(p1.y, p2.y)
            val bottom = maxOf(p1.y, p2.y)
            this.faceBox = RectF(left, top, right, bottom)
        } ?: run {
            this.faceBox = null
        }

        invalidate()
    }

    private fun translate(x: Float, y: Float, imageWidth: Int, imageHeight: Int): PointF {
        if (x < 0 || y < 0) return PointF(-1f, -1f)

        // Scaling factors (rotated 270 degrees for portrait front camera)
        val scaleX = width.toFloat() / imageHeight
        val scaleY = height.toFloat() / imageWidth

        // Horizontal mirroring + rotation translation
        val finalX = (imageHeight - y) * scaleX
        val finalY = x * scaleY
        return PointF(finalX, finalY)
    }

    fun clear() {
        this.faceBox = null
        this.eyeContactMaintained = null
        this.postureFeedback = null
        this.poseLandmarks = emptyList()
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Draw Pose Skeleton Connection Bones
        for (conn in connections) {
            if (conn.first < poseLandmarks.size && conn.second < poseLandmarks.size) {
                val start = poseLandmarks[conn.first]
                val end = poseLandmarks[conn.second]
                if (start.x > 0 && start.y > 0 && end.x > 0 && end.y > 0) {
                    canvas.drawLine(start.x, start.y, end.x, end.y, paintBone)
                }
            }
        }

        // Draw Pose Skeleton Joints
        for (landmark in poseLandmarks) {
            if (landmark.x > 0 && landmark.y > 0) {
                canvas.drawCircle(landmark.x, landmark.y, 14f, paintJoint)
            }
        }

        // Draw Face Bounding Box
        faceBox?.let { box ->
            canvas.drawRoundRect(box, 16f, 16f, paintFaceBox)
        }

        // Draw HUD Status Indicators Box (top-left)
        if (eyeContactMaintained != null || postureFeedback != null) {
            val bgRect = RectF(24f, 24f, 520f, 220f)
            canvas.drawRoundRect(bgRect, 16f, 16f, paintStatusBg)

            // Draw Eye Contact Status
            eyeContactMaintained?.let { eye ->
                canvas.drawText("Eye Contact:", 48f, 80f, paintTextTitle)
                if (eye) {
                    canvas.drawText("GOOD", 270f, 80f, paintTextGood)
                } else {
                    canvas.drawText("LOOK HERE", 270f, 80f, paintTextWarning)
                }
            }

            // Draw Posture Status
            postureFeedback?.let { feedback ->
                canvas.drawText("Posture:", 48f, 160f, paintTextTitle)
                if (feedback.equals("Good", ignoreCase = true)) {
                    canvas.drawText("GOOD", 200f, 160f, paintTextGood)
                } else {
                    canvas.drawText(feedback.uppercase(), 200f, 160f, paintTextWarning)
                }
            }
        }
    }
}
