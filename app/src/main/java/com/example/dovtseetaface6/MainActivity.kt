package com.example.dovtseetaface6

import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Process
import android.util.Log
import android.view.View
import android.widget.Button
import com.example.dovtseetaface6.seeta6.FaceDetector
import com.example.dovtseetaface6.seeta6.SeetaDevice
import com.example.dovtseetaface6.seeta6.SeetaImageData
import com.example.dovtseetaface6.seeta6.SeetaModelSetting
import java.util.jar.Manifest

class MainActivity : AppCompatActivity() {

    public var PERMISSION_REQ = 0x123456;

    private var mPermission = arrayOf<String>(
        android.Manifest.permission.CAMERA,
        android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
        android.Manifest.permission.READ_EXTERNAL_STORAGE,
        android.Manifest.permission.SYSTEM_ALERT_WINDOW
    )

    private var mRequestPermission = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

//        var faceDetector = FaceDetector(applicationContext)
//        faceDetector.loadEngine()
//        Log.d("dovt1: ", faceDetector.helloWorld().toString())

        if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M){
            for (one in mPermission){
                if (PackageManager.PERMISSION_GRANTED != this.checkPermission(one, Process.myPid(), Process.myUid())){
                    mRequestPermission.add(one)
                }
                if (!mRequestPermission.isEmpty()){
                    this.requestPermissions(mRequestPermission.toTypedArray(), PERMISSION_REQ)
                }
            }
        }
    }

    fun onClick(view: View){
        when(view.id){
            R.id.detect -> startActivity(Intent(this, FaceDetectorActivity::class.java))
            R.id.recognize -> startActivity(Intent(this, FaceRecognizerActivity::class.java))
        }
    }

}