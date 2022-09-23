package com.example.dovtseetaface6

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.hardware.Camera
import android.hardware.Camera.CameraInfo
import android.os.Bundle
import android.util.Log
import android.view.Surface
import android.widget.Button
import android.widget.FrameLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.dovtseetaface6.seeta6.FaceDetector

class FaceDetectorActivity : Activity() {
    private var TAG = "dovt1"

    private lateinit var btnCapture: Button
    private lateinit var container: FrameLayout

    private var mCamera: Camera? = null
    private var mPreview: CameraPreview? = null

    private lateinit var faceDetector: FaceDetector

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_face_detector)

        btnCapture = findViewById(R.id.btnCapture)
        container = findViewById(R.id.container)

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            mCamera = getCameraInstance()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 999)
        }

        mPreview = CameraPreview(this, mCamera)

        Log.w(TAG, "onCreate: " + mPreview )

        container.addView(mPreview)

    }

    /**
     * A safe way to get an instance of the Camera object.
     */
    fun getCameraInstance(): Camera? {
        var c: Camera? = null
        try {
            c = Camera.open(1) // attempt to get a Camera instance
            setCameraDisplayOrientation(
                this@FaceDetectorActivity,
                Camera.CameraInfo.CAMERA_FACING_FRONT,
                c
            )
        } catch (e: Exception) {
            // Camera is not available (in use or does not exist)
            Log.e("mess", e.message!!)
        }
        return c // returns null if camera is unavailable
    }

    fun setCameraDisplayOrientation(
        activity: FaceDetectorActivity,
        cameraId: Int, camera: Camera
    ) {
        val info = CameraInfo()
        Camera.getCameraInfo(cameraId, info)
        val rotation: Int = activity.getWindowManager().getDefaultDisplay().getRotation()
        var degrees = 0
        when (rotation) {
            Surface.ROTATION_0 -> degrees = 0
            Surface.ROTATION_90 -> degrees = 90
            Surface.ROTATION_180 -> degrees = 180
            Surface.ROTATION_270 -> degrees = 270
        }
        var result: Int
        if (info.facing == CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360
            result = (360 - result) % 360 // compensate the mirror
        } else { // back-facing
            result = (info.orientation - degrees + 360) % 360
        }
        camera.setDisplayOrientation(result)
    }

}