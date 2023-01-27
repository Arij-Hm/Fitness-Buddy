@file:Suppress("DEPRECATION")

package com.example.tracker

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler

class Splash : AppCompatActivity() {
    lateinit var handler: Handler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        handler= Handler()
        handler.postDelayed({
            val intent= Intent(this, Welcome::class.java)
            startActivity(intent)
            finish()
        }, 2000)
    }
}