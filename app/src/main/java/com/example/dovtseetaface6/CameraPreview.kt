package com.example.dovtseetaface6

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.hardware.Camera
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.example.dovtseetaface6.seeta6.FaceDetector
import com.example.dovtseetaface6.seeta6.SeetaImageData
import com.example.dovtseetaface6.seeta6.SeetaRect
import java.io.IOException


class CameraPreview : SurfaceView, SurfaceHolder.Callback {
    private var TAG = "dovt2"

    private val mHolder: SurfaceHolder
    private var mCamera: Camera? = null
    lateinit var faceDetector: FaceDetector
    var seetaRects: Array<SeetaRect?>? = null
    var seetaImageData: SeetaImageData? = null

    constructor(context: Context, mCamera: Camera?) : super(context) {
        this.mCamera = mCamera
        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mHolder = holder
        mHolder.addCallback(this)
        // deprecated setting, but required on Android versions prior to 3.0
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS)
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        // The Surface has been created, now tell the camera where to draw the preview.
        try {
            mCamera!!.setPreviewDisplay(holder)

            faceDetector = FaceDetector(context)
            faceDetector.loadEngine()
            val bmp = BitmapFactory.decodeResource(resources, R.drawable.face3)
            var face1Bytes = getNV21(bmp.width, bmp.height, bmp)

//            val parameters: Camera.Parameters = mCamera!!.getParameters()
//            parameters.setPreviewSize(640, 480)
//            mCamera!!.setParameters(parameters)
            Log.d(TAG, "surfaceCreated1: " + mCamera!!.parameters.previewSize.height)

            mCamera!!.setPreviewCallback { bytes, camera ->
                val time = System.currentTimeMillis()
                try {
                    //Log.w(TAG, "onPreviewFrame: time=" + time + ",len=" + bytes.size)
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                seetaImageData = SeetaImageData(1920, 1080, 1)
                seetaImageData!!.data = bytes.clone()
                Log.d(TAG, "surfaceCreated: " + bytes.size)
                Log.d(TAG, "surfaceCreated: " + seetaImageData!!.data.contentToString())
                seetaRects = faceDetector.Detect(seetaImageData)
                Log.d(TAG, "seetaRects: " + seetaRects?.size)
            }

            mCamera!!.startPreview()
        } catch (e: IOException) {
            Log.d("TAG", "Error setting camera preview: " + e.message)
        }
    }

    fun getNV21(inputWidth: Int, inputHeight: Int, scaled: Bitmap): ByteArray {
        val argb = IntArray(inputWidth * inputHeight)
        scaled.getPixels(argb, 0, inputWidth, 0, 0, inputWidth, inputHeight)
        val yuv = ByteArray(inputWidth * inputHeight * 3 / 2)
        encodeYUV420SP(yuv, argb, inputWidth, inputHeight)
        scaled.recycle()
        return yuv
    }

    fun encodeYUV420SP(yuv420sp: ByteArray, argb: IntArray, width: Int, height: Int) {
        val frameSize = width * height
        var yIndex = 0
        var uvIndex = frameSize
        var a: Int
        var R: Int
        var G: Int
        var B: Int
        var Y: Int
        var U: Int
        var V: Int
        var index = 0
        for (j in 0 until height) {
            for (i in 0 until width) {
                a = argb[index] and -0x1000000 shr 24 // a is not used obviously
                R = argb[index] and 0xff0000 shr 16
                G = argb[index] and 0xff00 shr 8
                B = argb[index] and 0xff shr 0

// well known RGB to YUV algorithm
                Y = (66 * R + 129 * G + 25 * B + 128 shr 8) + 16
                U = (-38 * R - 74 * G + 112 * B + 128 shr 8) + 128
                V = (112 * R - 94 * G - 18 * B + 128 shr 8) + 128

// NV21 has a plane of Y and interleaved planes of VU each sampled by a factor of 2

// meaning for every 4 Y pixels there are 1 V and 1 U. Note the sampling is every other

// pixel AND every other scanline.
                yuv420sp[yIndex++] = (if (Y < 0) 0 else if (Y > 255) 255 else Y).toByte()
                if (j % 2 == 0 && index % 2 == 0) {
                    yuv420sp[uvIndex++] = (if (V < 0) 0 else if (V > 255) 255 else V).toByte()
                    yuv420sp[uvIndex++] = (if (U < 0) 0 else if (U > 255) 255 else U).toByte()
                }
                index++
            }
        }
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        // empty. Take care of releasing the Camera preview in your activity.
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, w: Int, h: Int) {
        // If your preview can change or rotate, take care of those events here.
        // Make sure to stop the preview before resizing or reformatting it.
        if (mHolder.surface == null) {
            // preview surface does not exist
            return
        }

        // stop preview before making changes
        try {
            mCamera!!.stopPreview()
        } catch (e: Exception) {
            // ignore: tried to stop a non-existent preview
        }

        // set preview size and make any resize, rotate or
        // reformatting changes here

        // start preview with new settings
        try {
            mCamera!!.setPreviewDisplay(mHolder)
            mCamera!!.startPreview()
        } catch (e: Exception) {
            Log.d("TAG", "Error starting camera preview: " + e.message)
        }
    }
}
