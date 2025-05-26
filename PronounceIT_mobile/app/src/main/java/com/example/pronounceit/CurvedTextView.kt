package com.example.pronounceit

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.animation.AlphaAnimation

class CurvedTextView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#F3AD00")
        textSize = 40f
        style = Paint.Style.FILL
        typeface = Typeface.DEFAULT_BOLD
        textAlign = Paint.Align.CENTER
    }
    private val path = Path()
    private val text = "Press Me"

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val width = width.toFloat()
        val height = height.toFloat()
        val radius = width / 2.2f
        val centerX = width / 2
        val centerY = height

        // Draw a semi-circular arc (curve above the mic)
        path.reset()
        path.addArc(
            centerX - radius, centerY - radius,
            centerX + radius, centerY + radius,
            200f, 140f
        )
        canvas.drawTextOnPath(text, path, 0f, 0f, paint)
    }

    fun startBlinkAnimation() {
        val anim = AlphaAnimation(1.0f, 0.3f)
        anim.duration = 600
        anim.repeatMode = AlphaAnimation.REVERSE
        anim.repeatCount = AlphaAnimation.INFINITE
        startAnimation(anim)
    }

    fun stopBlinkAnimation() {
        clearAnimation()
    }
}