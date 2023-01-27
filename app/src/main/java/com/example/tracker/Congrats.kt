package com.example.tracker

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.animation.DecelerateInterpolator
import android.widget.Button
import androidx.core.graphics.ColorUtils
import kotlinx.android.synthetic.main.activity_congrats.*

class Congrats : AppCompatActivity() {

    private var popupTitle = ""
    private var popupText = ""
    private var popupButton = ""
    private var darkStatusBar = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overridePendingTransition(0, 0)
        setContentView(R.layout.activity_congrats)

        val close : Button = findViewById(R.id.popup_window_button)
        close.setOnClickListener {
            val alpha = 100 // between 0-255
            val alphaColor = ColorUtils.setAlphaComponent(Color.parseColor("#000000"), alpha)
            val colorAnimation = ValueAnimator.ofObject(ArgbEvaluator(), alphaColor, Color.TRANSPARENT)
            colorAnimation.duration = 500 // milliseconds
            colorAnimation.addUpdateListener { animator ->
                popup_window_background.setBackgroundColor(
                    animator.animatedValue as Int
                )
            }

            // Fade animation for the Popup Window when you press the back button
            popup_window_view_with_border.animate().alpha(0f).setDuration(500).setInterpolator(
                DecelerateInterpolator()
            ).start()

            // After animation finish, close the Activity
            colorAnimation.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    finish()
                    overridePendingTransition(0, 0)
                }
            })
            colorAnimation.start()
        }

        val alpha = 100 //between 0-255
        val alphaColor = ColorUtils.setAlphaComponent(Color.parseColor("#000000"), alpha)
        val colorAnimation = ValueAnimator.ofObject(ArgbEvaluator(), Color.TRANSPARENT, alphaColor)
        colorAnimation.duration = 500 // milliseconds
        colorAnimation.addUpdateListener { animator ->
            popup_window_background.setBackgroundColor(animator.animatedValue as Int)
        }
        colorAnimation.start()

    }

    override fun onBackPressed() {
        // Fade animation for the background of Popup Window when you press the back button
        val alpha = 100 // between 0-255
        val alphaColor = ColorUtils.setAlphaComponent(Color.parseColor("#000000"), alpha)
        val colorAnimation = ValueAnimator.ofObject(ArgbEvaluator(), alphaColor, Color.TRANSPARENT)
        colorAnimation.duration = 500 // milliseconds
        colorAnimation.addUpdateListener { animator ->
            popup_window_background.setBackgroundColor(
                animator.animatedValue as Int
            )
        }

        // Fade animation for the Popup Window when you press the back button
        popup_window_view_with_border.animate().alpha(0f).setDuration(500).setInterpolator(
            DecelerateInterpolator()
        ).start()

        // After animation finish, close the Activity
        colorAnimation.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                finish()
                overridePendingTransition(0, 0)
            }
        })
        colorAnimation.start()
    }
}