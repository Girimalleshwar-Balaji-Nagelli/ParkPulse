package com.parkpulse.app.presentation.camera

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import androidx.compose.ui.graphics.toArgb
import com.parkpulse.app.presentation.theme.ParksColors
import kotlin.math.max
import kotlin.math.min

/** Burns the stamp into the pixels, so it is permanent and survives any later copy or share. */
object PhotoStamper {

    /** Returns a NEW rotated + stamped bitmap. The caller still owns (and should recycle) [source]. */
    fun stamp(source: Bitmap, rotationDegrees: Int, data: StampData): Bitmap {
        val matrix = Matrix().apply { postRotate(rotationDegrees.toFloat()) }
        val rotated = Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
        val out = rotated.copy(Bitmap.Config.ARGB_8888, true)
        if (rotated !== source) rotated.recycle()
        drawStamp(Canvas(out), out.width, out.height, data)
        return out
    }

    private fun drawStamp(canvas: Canvas, w: Int, h: Int, data: StampData) {
        val u = min(w, h) / 36f // everything scales with the photo size
        val pad = 0.8f * u
        val margin = 1.0f * u
        val gap = 0.35f * u

        val bg = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = ParksColors.White.copy(alpha = 0.88f).toArgb() }
        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = ParksColors.Black.toArgb(); textSize = 1.25f * u; typeface = Typeface.DEFAULT_BOLD
        }
        val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = ParksColors.Black.toArgb(); textSize = 1.0f * u
        }

        val maxText = w - 2 * (margin + pad)
        val raw = StampText.lines(data)
        val paints = listOf(titlePaint, bodyPaint, bodyPaint)
        val lines = raw.mapIndexed { i, text -> fit(text, paints[i], maxText) }

        val lineHeights = paints.map { it.fontMetrics.let { fm -> fm.descent - fm.ascent } }
        val boxW = lines.indices.maxOf { paints[it].measureText(lines[it]) } + 2 * pad
        val boxH = 2 * pad + lineHeights.sum() + gap * (lines.size - 1)

        val left = margin
        val bottom = h - margin
        val top = bottom - boxH
        canvas.drawRoundRect(RectF(left, top, left + boxW, bottom), 0.7f * u, 0.7f * u, bg)

        var y = top + pad
        lines.forEachIndexed { i, text ->
            canvas.drawText(text, left + pad, y - paints[i].fontMetrics.ascent, paints[i])
            y += lineHeights[i] + gap
        }
    }

    private fun fit(text: String, paint: Paint, maxWidth: Float): String {
        val count = paint.breakText(text, true, maxWidth, null)
        return if (count >= text.length) text else text.take(max(0, count - 1)) + "…"
    }
}
