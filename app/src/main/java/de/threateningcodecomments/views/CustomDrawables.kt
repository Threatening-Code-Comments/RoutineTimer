package de.threateningcodecomments.accessibility

import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.drawable.Drawable
import androidx.core.graphics.drawable.toBitmap


class TextDrawable(val color: Int, val message: String) : Drawable() {
    private var cf = this.colorFilter

    override fun draw(canvas: Canvas) {
        val paint = Paint().apply {
            color = this@TextDrawable.color
            textSize = 55f
            isFakeBoldText = true
            textAlign = Paint.Align.CENTER
            colorFilter = cf
        }
        canvas.drawText(message, 73f, 75f, paint)
    }

    override fun getOpacity(): Int = PixelFormat.OPAQUE

    override fun setAlpha(arg0: Int) {}

    override fun setColorFilter(cf: ColorFilter?) {
        this.cf = cf
    }
}

class CombinedDrawable(private val d1: Drawable?, private val d2: Drawable) : Drawable() {
    private var cf = this.colorFilter

    override fun draw(canvas: Canvas) {

        val b1 = d1?.toBitmap(100, 100, null)
        val b2 = d2.toBitmap(100, 100, null)

        val paint = Paint().apply { colorFilter = cf }

        if (b1 != null)
            canvas.drawBitmap(b1, 25f, 5f, paint)
        canvas.drawBitmap(b2, 0f, 0f, paint)
    }

    override fun getOpacity(): Int = PixelFormat.OPAQUE

    override fun setAlpha(arg0: Int) {}

    override fun setColorFilter(cf: ColorFilter?) {
        this.cf = cf
    }
}