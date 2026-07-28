package com.example.aiinterviewcoach.vision

import android.graphics.PointF
import android.graphics.Rect
import android.graphics.RectF
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.google.mlkit.vision.pose.PoseDetection
import com.google.mlkit.vision.pose.PoseLandmark
import com.google.mlkit.vision.pose.defaults.PoseDetectorOptions
import java.util.concurrent.atomic.AtomicBoolean

class FacePoseAnalyzer(
    private val onAnalysisResult: (Result) -> Unit
) : ImageAnalysis.Analyzer {

    data class Result(
        val faceBox: Rect?,
        val eyeContactMaintained: Boolean,
        val postureFeedback: String,
        val poseLandmarks: List<PointF>,
        val imageWidth: Int,
        val imageHeight: Int,
        val rotationDegrees: Int
    )

    // ML Kit Face Detector options (Fast mode, classifications enabled for eye open checks)
    private val faceOptions = FaceDetectorOptions.Builder()
        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
        .build()
    private val faceDetector = FaceDetection.getClient(faceOptions)

    // ML Kit Pose Detector options (Stream mode for continuous frames)
    private val poseOptions = PoseDetectorOptions.Builder()
        .setDetectorMode(PoseDetectorOptions.STREAM_MODE)
        .build()
    private val poseDetector = PoseDetection.getClient(poseOptions)

    // Metrics tracking state (for final session summary logging/scoring)
    private val isTracking = AtomicBoolean(false)
    private var totalFrames = 0L
    private var eyeContactFrames = 0L
    private var goodPostureFrames = 0L

    fun startTracking() {
        totalFrames = 0
        eyeContactFrames = 0
        goodPostureFrames = 0
        isTracking.set(true)
    }

    fun stopTracking(): Summary {
        isTracking.set(false)
        val eyeScore = if (totalFrames > 0) (eyeContactFrames * 100 / totalFrames).toInt() else 100
        val postureScore = if (totalFrames > 0) (goodPostureFrames * 100 / totalFrames).toInt() else 100
        return Summary(eyeScore, postureScore, totalFrames)
    }

    data class Summary(
        val eyeContactPercentage: Int,
        val posturePercentage: Int,
        val totalFramesAnalyzed: Long
    )

    @OptIn(ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage == null) {
            imageProxy.close()
            return
        }

        val rotation = imageProxy.imageInfo.rotationDegrees
        val inputImage = InputImage.fromMediaImage(mediaImage, rotation)

        val faceTask = faceDetector.process(inputImage)
        val poseTask = poseDetector.process(inputImage)

        // Process both detectors asynchronously and close imageProxy when both complete
        Tasks.whenAllComplete(faceTask, poseTask).addOnCompleteListener {
            val faces = if (faceTask.isSuccessful) faceTask.result else null
            val pose = if (poseTask.isSuccessful) poseTask.result else null

            // 1. Check Eye Contact
            val face = faces?.firstOrNull()
            var eyeContact = false
            var faceBox: Rect? = null

            if (face != null) {
                faceBox = face.boundingBox
                val yaw = face.headEulerAngleY
                val pitch = face.headEulerAngleX
                val leftEye = face.leftEyeOpenProbability ?: 1.0f
                val rightEye = face.rightEyeOpenProbability ?: 1.0f

                // Thresholds: Head facing forward (within 15 degrees) & eyes open
                eyeContact = Math.abs(yaw) < 15f && Math.abs(pitch) < 15f && leftEye > 0.5f && rightEye > 0.5f
            }

            // 2. Check Posture
            var postureFeedback = "Good"
            val rawLandmarks = mutableListOf<PointF>()

            if (pose != null) {
                // Relevant body posture landmarks
                val leftShoulder = pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER)
                val rightShoulder = pose.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER)
                val leftElbow = pose.getPoseLandmark(PoseLandmark.LEFT_ELBOW)
                val rightElbow = pose.getPoseLandmark(PoseLandmark.RIGHT_ELBOW)
                val leftWrist = pose.getPoseLandmark(PoseLandmark.LEFT_WRIST)
                val rightWrist = pose.getPoseLandmark(PoseLandmark.RIGHT_WRIST)
                val leftHip = pose.getPoseLandmark(PoseLandmark.LEFT_HIP)
                val rightHip = pose.getPoseLandmark(PoseLandmark.RIGHT_HIP)

                val activeLandmarks = listOf(
                    leftShoulder, rightShoulder, leftElbow, rightElbow,
                    leftWrist, rightWrist, leftHip, rightHip
                )

                for (landmark in activeLandmarks) {
                    if (landmark != null && landmark.inFrameLikelihood > 0.5f) {
                        rawLandmarks.add(PointF(landmark.position.x, landmark.position.y))
                    } else {
                        rawLandmarks.add(PointF(-1f, -1f)) // Out of frame
                    }
                }

                // Check shoulder tilt
                if (leftShoulder != null && rightShoulder != null &&
                    leftShoulder.inFrameLikelihood > 0.5f && rightShoulder.inFrameLikelihood > 0.5f
                ) {
                    val shoulderSlope = Math.abs(leftShoulder.position.y - rightShoulder.position.y)

                    if (shoulderSlope > 45f) {
                        postureFeedback = "Shoulders slanted"
                    } else {
                        // Check Slouching (Nose position compared to the center-line height of shoulders)
                        val nose = pose.getPoseLandmark(PoseLandmark.NOSE)
                        if (nose != null && nose.inFrameLikelihood > 0.5f) {
                            val averageShoulderY = (leftShoulder.position.y + rightShoulder.position.y) / 2
                            val noseToShoulderHeight = averageShoulderY - nose.position.y

                            if (noseToShoulderHeight < 110f) {
                                postureFeedback = "Slouching"
                            }
                        }
                    }
                }
            }

            // 3. Track Session Stats
            if (isTracking.get()) {
                totalFrames++
                if (eyeContact) eyeContactFrames++
                if (postureFeedback.equals("Good", ignoreCase = true)) goodPostureFrames++
            }

            // 4. Return coordinates and metrics to UI callback
            onAnalysisResult(
                Result(
                    faceBox = faceBox,
                    eyeContactMaintained = eyeContact,
                    postureFeedback = postureFeedback,
                    poseLandmarks = rawLandmarks,
                    imageWidth = imageProxy.width,
                    imageHeight = imageProxy.height,
                    rotationDegrees = rotation
                )
            )

            // Release frame
            imageProxy.close()
        }
    }
}
