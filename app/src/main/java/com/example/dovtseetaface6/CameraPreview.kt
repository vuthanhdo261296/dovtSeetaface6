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
import android.view.WindowManager
import androidx.annotation.RequiresApi
import com.example.dovtseetaface6.seeta6.*
import java.io.IOException


class CameraPreview : SurfaceView, SurfaceHolder.Callback {
    private var TAG = "dovt2"

    lateinit var surfaceView: SurfaceView

    private val mHolder: SurfaceHolder
    private var mCamera: Camera? = null
    lateinit var faceDetector: FaceDetector
    lateinit var faceTracker: FaceTracker
    var seetaRects: Array<SeetaRect?>? = null
    var seetaTrackingFaceInfos: Array<SeetaTrackingFaceInfo?>? = null
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
//            val bmp = BitmapFactory.decodeResource(resources, R.drawable.face3)
//            var face1Bytes = getNV21(bmp.width, bmp.height, bmp)
            var yuvType: Type.Builder? = null
            var rgbaType: Type.Builder? = null
            var rs: RenderScript? = null
            var `in`: Allocation? = null
            var out:Allocation? = null
            var yuvToRgbIntrinsic: ScriptIntrinsicYuvToRGB? = null
            var displayOrientation: Int? = null

            rs = RenderScript.create(context)
            yuvToRgbIntrinsic = ScriptIntrinsicYuvToRGB.create(rs, Element.U8_4(rs))

            mCamera!!.setPreviewDisplay(holder)

            val cameraWidth = 640
            val cameraHeight = 480
            val parameters: Camera.Parameters = mCamera!!.getParameters()
            parameters.setPreviewSize(cameraWidth, cameraHeight)
            mCamera!!.setParameters(parameters)

            //faceDetector = FaceDetector(context)
            faceTracker = FaceTracker(context, cameraWidth, cameraHeight)

            //faceDetector.set(FaceDetector.Property.PROPERTY_MIN_FACE_SIZE, 20.0)
            //Log.d(TAG, "surfaceCreatedMinFaceSize: " + faceDetector.get(FaceDetector.Property.PROPERTY_MIN_FACE_SIZE))

            displayOrientation = getCameraOri(context.display!!.rotation)

            Log.d(TAG, "surfaceCreatedrotation: " + context.display!!.rotation)

            val metrics = DisplayMetrics()
            context.display?.getMetrics(metrics)
            Log.d(TAG, "metrics: " + metrics.widthPixels + " " + metrics.heightPixels)




            Log.w(TAG,"surfaceCreated: w=$cameraWidth,h=$cameraHeight")

            mCamera!!.setPreviewCallback { bytes, camera ->
                val time = System.currentTimeMillis()
//                try {
//                    Log.w(TAG,"onPreviewFrame: time=" + time + ",len=" + bytes.size)
//                    if (yuvType == null) {
//                        yuvType = Type.Builder(rs, Element.U8(rs)).setX(bytes.size)
//                        `in` = Allocation.createTyped(rs, yuvType?.create(), Allocation.USAGE_SCRIPT)
//                        rgbaType = Type.Builder(rs, Element.RGBA_8888(rs)).setX(cameraWidth).setY(cameraHeight)
//                        out = Allocation.createTyped(rs, rgbaType?.create(), Allocation.USAGE_SCRIPT)
//                    }
//                    `in`?.copyFrom(bytes)
//                    yuvToRgbIntrinsic.setInput(`in`)
//                    yuvToRgbIntrinsic.forEach(out)
//                } catch (e: Exception) {
//                    e.printStackTrace()
//                }
//                var bitmap = Bitmap.createBitmap(cameraWidth, cameraHeight, Bitmap.Config.ARGB_8888)
//                out?.copyTo(bitmap)
//                Log.d(TAG, "surfaceCreatedbitmap: " + bitmap.hasAlpha())
//                Log.d(TAG, "surfaceCreatedbitmap: " + bytes.size)
//                val byteBuffer = ByteBuffer.allocate(bitmap.byteCount)
//                bitmap.copyPixelsToBuffer(byteBuffer)
//                byteBuffer.rewind()
//                var newBytes = byteBuffer.array()
                var newBytes = yuv2rgb(bytes, cameraWidth, cameraHeight)

                seetaImageData = SeetaImageData(cameraWidth, cameraHeight,3)
                seetaImageData!!.data = newBytes.clone()

                Log.d(TAG,"onPreviewFrameD: timeD1=" + (System.currentTimeMillis() - time))
                //seetaRects = faceDetector.Detect(seetaImageData)
                seetaTrackingFaceInfos = faceTracker.Track(seetaImageData)
                Log.d(TAG,"onPreviewFrameD: timeD2=" + (System.currentTimeMillis() - time))

                //Log.d(TAG, "seetaRects: " + seetaRects?.size)
                Log.d(TAG, "seetaTrackingFaceInfos: " + seetaTrackingFaceInfos?.size)

                if (seetaTrackingFaceInfos?.size!! > 0) {

                    Log.w(TAG,"onPreviewFrame: time=" + (System.currentTimeMillis() - time))
                    for (stTFInfo in seetaTrackingFaceInfos!!){
                        Log.d(TAG, "surfaceCreatedseetaRect: " + stTFInfo!!.x + " " + stTFInfo.y + " " + stTFInfo.width +" "  +  stTFInfo.height)
                        val maxRect = Rect(stTFInfo!!.x, stTFInfo.y, stTFInfo.width + stTFInfo.x, stTFInfo.height + stTFInfo.y)
//                        if (maxRect.left - 10 >= 0) {
//                            maxRect.left -= 10
//                        }
//                        if (maxRect.right + 20 <= cameraWidth) {
//                            maxRect.right += 20
//                        }
//                        if (maxRect.top - 10 >= 0) {
//                            maxRect.top -= 10
//                        }
//                        if (maxRect.bottom <= cameraHeight) {
//                            maxRect.bottom += 20
//                        }
//                        maxRect.left += 10
//                        maxRect.right -= 10
//                        maxRect.top += 10
//                        maxRect.bottom -= 10

                        Log.d(TAG, "maxRect: " + maxRect.left + " " + maxRect.top + " " + maxRect.right +" " +  maxRect.bottom)

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
                                        //bitmap = Bitmap.createBitmap(bitmap, rect!!.left, rect!!.top, width, height)

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
                                            DrawUtils().drawFaceRect(canvas, adjustedRect, Color.WHITE,5)
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

    fun yuv2rgb(yuv: ByteArray, width: Int, height: Int): ByteArray {
        val total = width * height
        var listRgb: MutableList<Byte> = arrayListOf()
        //val rgb = ByteArray(total)
        var Y: Int
        var Cb = 0
        var Cr = 0
        //var index = 0
        var R: Int
        var G: Int
        var B: Int
        for (y in 0 until height) {
            for (x in 0 until width) {
                Y = yuv[y * width + x].toInt()
                if (Y < 0) Y += 255
                if (x and 1 == 0) {
                    Cr = yuv[(y shr 1) * width + x + total].toInt()
                    Cb = yuv[(y shr 1) * width + x + total + 1].toInt()
                    if (Cb < 0) Cb += 127 else Cb -= 128
                    if (Cr < 0) Cr += 127 else Cr -= 128
                }
//                R = Y + Cr + (Cr shr 2) + (Cr shr 3) + (Cr shr 5)
//                G =
//                    Y - (Cb shr 2) + (Cb shr 4) + (Cb shr 5) - (Cr shr 1) + (Cr shr 3) + (Cr shr 4) + (Cr shr 5)
//                B = Y + Cb + (Cb shr 1) + (Cb shr 2) + (Cb shr 6)

                // Approximation
				R = ((Y + 1.40200 * Cr).toInt());
			    G =  ((Y - 0.34414 * Cb - 0.71414 * Cr).toInt());
				B =  ((Y + 1.77200 * Cb).toInt());

                if (R < 0) R = 0 else if (R > 255) R = 255
                if (G < 0) G = 0 else if (G > 255) G = 255
                if (B < 0) B = 0 else if (B > 255) B = 255
                //rgb[index++] = -0x1000000 + (R shl 16) + (G shl 8) + B
                listRgb?.add(B.toByte())
                listRgb?.add(G.toByte())
                listRgb?.add(R.toByte())
            }
        }
        return listRgb.toByteArray()
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
