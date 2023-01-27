package com.example.tracker

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.tbruyelle.rxpermissions2.RxPermissions

@Suppress("DEPRECATION")
class Welcome : AppCompatActivity() {

    @SuppressLint("CheckResult")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        val button = findViewById<Button>(R.id.GetStarted)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(baseContext,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION, Manifest.permission.ACTIVITY_RECOGNITION),
                    99)
                Toast.makeText(this@Welcome, "Permission is necessary.", Toast.LENGTH_SHORT).show()
            }else{
                button.setOnClickListener {
                    val intent = Intent(this, LoginActivity::class.java)
                    startActivity(intent)
                }
            }
        }

        RxPermissions(this).request(/*Manifest.permission.ACTIVITY_RECOGNITION*/ Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.ACCESS_FINE_LOCATION)
            .subscribe { isGranted ->
                if (isGranted) {
                    //Log.d("TAG", "Is ACTIVITY_RECOGNITION permission granted: $isGranted")
                    button.setOnClickListener {
                        val intent = Intent(this, LoginActivity::class.java)
                        startActivity(intent)
                    }
                } else {
                    Toast.makeText(this@Welcome, "Permission is necessary.", Toast.LENGTH_SHORT).show()
                }
            }
    }
}