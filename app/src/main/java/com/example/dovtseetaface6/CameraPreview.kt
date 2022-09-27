package com.example.dovtseetaface6

import android.content.Context
import android.graphics.*
import android.hardware.Camera
import android.hardware.Camera.CameraInfo
import android.os.Build
import android.renderscript.*
import android.util.DisplayMetrics
import android.util.Log
import android.view.Surface
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.annotation.RequiresApi
import com.example.dovtseetaface6.seeta6.FaceDetector
import com.example.dovtseetaface6.seeta6.SeetaImageData
import com.example.dovtseetaface6.seeta6.SeetaRect
import java.io.IOException


class CameraPreview : SurfaceView, SurfaceHolder.Callback {
    private var TAG = "dovt2"

    lateinit var surfaceView: SurfaceView

    private val mHolder: SurfaceHolder
    private var mCamera: Camera? = null
    lateinit var faceDetector: FaceDetector
    var seetaRects: Array<SeetaRect?>? = null
    var seetaImageData: SeetaImageData? = null
    private var canvas: Canvas? = null

    constructor(context: Context, mCamera: Camera?) : super(context) {
        this.mCamera = mCamera
        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mHolder = holder
        mHolder.addCallback(this)
        // deprecated setting, but required on Android versions prior to 3.0
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS)
    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun surfaceCreated(holder: SurfaceHolder) {
        // The Surface has been created, now tell the camera where to draw the preview.
        try {
            mCamera!!.setPreviewDisplay(holder)

            faceDetector = FaceDetector(context)
            faceDetector.loadEngine()
            val bmp = BitmapFactory.decodeResource(resources, R.drawable.face3)
            var face1Bytes = getNV21(bmp.width, bmp.height, bmp)
            var yuvType: Type.Builder? = null
            var rgbaType: Type.Builder? = null
            var rs: RenderScript? = null
            var `in`: Allocation? = null
            var out:Allocation? = null
            var yuvToRgbIntrinsic: ScriptIntrinsicYuvToRGB? = null
            var displayOrientation: Int? = null

            rs = RenderScript.create(context)
            yuvToRgbIntrinsic = ScriptIntrinsicYuvToRGB.create(rs, Element.U8_4(rs))

            displayOrientation = getCameraOri(context.display!!.rotation)
            Log.d(TAG, "surfaceCreatedrotation: " + context.display!!.rotation)
            val cameraWidth = 1920
            val cameraHeight = 1080
            val metrics = DisplayMetrics()
            context.display?.getMetrics(metrics)
            val parameters: Camera.Parameters = mCamera!!.getParameters()
            parameters.setPreviewSize(cameraWidth, cameraHeight)
            mCamera!!.setParameters(parameters)

            Log.w(TAG,"surfaceCreated: w=$cameraWidth,h=$cameraHeight")

            mCamera!!.setPreviewCallback { bytes, camera ->
                val time = System.currentTimeMillis()
                try {
                    Log.w(TAG,"onPreviewFrame: time=" + time + ",len=" + bytes.size)
                    if (yuvType == null) {
                        yuvType = Type.Builder(rs, Element.U8(rs)).setX(bytes.size)
                        `in` = Allocation.createTyped(rs, yuvType?.create(), Allocation.USAGE_SCRIPT)
                        rgbaType = Type.Builder(rs, Element.RGBA_8888(rs)).setX(cameraWidth).setY(cameraHeight)
                        out = Allocation.createTyped(rs, rgbaType?.create(), Allocation.USAGE_SCRIPT)
                    }
                    `in`?.copyFrom(bytes)
                    yuvToRgbIntrinsic.setInput(`in`)
                    yuvToRgbIntrinsic.forEach(out)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                var bitmap = Bitmap.createBitmap(cameraWidth, cameraHeight, Bitmap.Config.ARGB_8888)
                out?.copyTo(bitmap)

                seetaImageData = SeetaImageData(cameraWidth, cameraHeight)
                seetaImageData!!.data = bytes.clone()
                Log.d(TAG, "surfaceCreated: " + bytes.size)
                Log.d(TAG, "surfaceCreated: " + seetaImageData!!.data.contentToString())
                seetaRects = faceDetector.Detect(seetaImageData)
                Log.d(TAG, "seetaRects: " + seetaRects?.size)

                if (seetaRects?.size!! > 0) {

                    Log.w(TAG,"onPreviewFrame: time=" + (System.currentTimeMillis() - time))
                    for (seetaRect in seetaRects!!){
                        Log.d(TAG, "surfaceCreatedseetaRect: " + seetaRect!!.x + " " + seetaRect.y + " " + seetaRect.width +" "  +  seetaRect.height)
                        val maxRect = Rect(seetaRect!!.x, seetaRect.y, seetaRect.width + seetaRect.x, seetaRect.height + seetaRect.y)
                        if (maxRect.left - 10 >= 0) {
                            maxRect.left -= 10
                        }
                        if (maxRect.right + 20 <= cameraWidth) {
                            maxRect.right += 20
                        }
                        if (maxRect.top - 10 >= 0) {
                            maxRect.top -= 10
                        }
                        if (maxRect.bottom <= cameraHeight) {
                            maxRect.bottom += 20
                        }

                        Log.d(TAG, "surfaceCreatedseetaRect: " + seetaRect!!.x + " " + seetaRect.y + " " + seetaRect.width +" " +  seetaRect.height)
                        Log.w(TAG,"onPreviewFrame: time2=" + (System.currentTimeMillis() - time))
                        Log.d(TAG, "surfaceCreatedsurfaceView: " + surfaceView)
                        if (surfaceView != null) {
                            canvas = null
                            surfaceView?.setVisibility(VISIBLE)
                            try {
                                canvas = surfaceView?.getHolder()?.lockCanvas()
                                Log.w(TAG,"onPreviewFrame: canvas=$canvas")
                                if (canvas != null) {
                                    synchronized(surfaceView!!.holder) {
                                        canvas!!.drawColor(0, PorterDuff.Mode.CLEAR)
                                        val rect = Rect(maxRect.left, maxRect.top, maxRect.right, maxRect.bottom)
                                        val width = rect!!.right - rect!!.left
                                        val height = rect!!.bottom - rect!!.top
                                        bitmap = Bitmap.createBitmap(bitmap, rect!!.left, rect!!.top, width, height)

                                        Log.w(TAG,"onPreviewFrame: time3=" + (System.currentTimeMillis() - time))
                                        if (rect != null) {
                                            val adjustedRect: Rect? = DrawUtils().adjustRect(
                                                rect,
                                                cameraWidth,
                                                cameraHeight,
                                                canvas!!.getWidth(),
                                                canvas!!.getHeight(),
                                                displayOrientation,
                                                1
                                            )
                                            Log.d(TAG, "surfaceCreatedcanvas: " + canvas!!.width + " " + canvas!!.height + " " + displayOrientation)
                                            Log.d(TAG, "surfaceCreatedadjustedRect: " + adjustedRect!!.left + " " + adjustedRect!!.top + " " + adjustedRect!!.right + " " + adjustedRect!!.bottom)
                                            //画人脸框
                                            DrawUtils().drawFaceRect(canvas, adjustedRect, Color.YELLOW,5)
                                            Log.w(TAG,"onPreviewFrame: time4=" + (System.currentTimeMillis() - time))
                                        }
                                    }
                                }
                            } catch (ex: Exception) {
                            } finally {
                                if (canvas != null) {
                                    surfaceView?.getHolder()?.unlockCanvasAndPost(canvas)
                                }
                            }
                        }


                    }



                } else {
                    if (surfaceView != null) {
                        surfaceView!!.setVisibility(INVISIBLE)
                    }
                }

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

    private fun getCameraOri(rotation: Int): Int {
        var camereId = 1 //front
        var degrees = rotation * 90
        when (rotation) {
            Surface.ROTATION_0 -> degrees = 0
            Surface.ROTATION_90 -> degrees = 90
            Surface.ROTATION_180 -> degrees = 180
            Surface.ROTATION_270 -> degrees = 270
            else -> {}
        }
        var result: Int
        val info = CameraInfo()
        Camera.getCameraInfo(camereId, info)
        if (info.facing == CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360
            result = (360 - result) % 360
        } else {
            result = (info.orientation - degrees + 360) % 360
        }
        return result
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
