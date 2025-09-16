package com.devsneha.snappy.util

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.devsneha.snappy.model.SnappyTool

fun snapPoint(p: Offset, last: Offset?): Offset {
    val grid = 16f
    val gx = (p.x / grid).toInt() * grid
    val gy = (p.y / grid).toInt() * grid
    val dxg = p.x - gx
    val dyg = p.y - gy
    val gridThreshold = 6f
    var candidate = Offset(
        if (kotlin.math.abs(dxg) < gridThreshold) gx else p.x,
        if (kotlin.math.abs(dyg) < gridThreshold) gy else p.y
    )

    if (last != null) {
        val v = p - last
        val dist = v.getDistance()
        if (dist > 0f) {
            val axisThreshold = 10f
            if (kotlin.math.abs(v.y) < axisThreshold) candidate = Offset(p.x, last.y)
            if (kotlin.math.abs(v.x) < axisThreshold) candidate = Offset(last.x, p.y)

            val angle = kotlin.math.atan2(v.y, v.x)
            val step = (Math.PI / 4.0).toFloat()
            val snappedAngle = (kotlin.math.round(angle / step) * step)
            val projected = Offset(
                last.x + kotlin.math.cos(snappedAngle) * dist,
                last.y + kotlin.math.sin(snappedAngle) * dist
            )
            val projDelta = (projected - p)
            if (projDelta.getDistance() < 14f) candidate = projected
        }
    }
    return candidate
}


fun snapConstrainedLine(p: Offset, start: Offset, constrainPerpendicular: Boolean): Offset {
    val base = p - start
    val dist = base.getDistance().coerceAtLeast(1f)
    var angle = kotlin.math.atan2(base.y, base.x)

    val step = (Math.PI / 4.0).toFloat()
    angle = (kotlin.math.round(angle / step) * step)

    if (constrainPerpendicular) {
        angle += (Math.PI / 2.0).toFloat()
    }
    val end = Offset(
        start.x + kotlin.math.cos(angle) * dist,
        start.y + kotlin.math.sin(angle) * dist
    )
    return snapPoint(end, start)
}

fun projectOntoRuler(p: Offset, center: Offset, angle: Float): Offset {
    val dir = Offset(kotlin.math.cos(angle), kotlin.math.sin(angle))
    val v = p - center
    val t = (v.x * dir.x + v.y * dir.y)
    return center + dir * t
}

fun snapAngle(angle: Float): Float {
    val allowed = floatArrayOf(0f, 30f, 45f, 60f, 90f)
    val twoPi = (Math.PI * 2).toFloat()
    var a = angle % twoPi
    if (a < 0) a += twoPi
    val deg = Math.toDegrees(a.toDouble()).toFloat()
    var best = a
    var bestDiff = Float.MAX_VALUE
    for (base in allowed) {
        var k = 0f
        while (k <= 360f) {
            val candDeg = (base + k) % 360f
            val diff = kotlin.math.abs(((deg - candDeg + 540f) % 360f) - 180f)
            if (diff < bestDiff) {
                bestDiff = diff
                best = Math.toRadians(candDeg.toDouble()).toFloat()
            }
            k += base.coerceAtLeast(30f)
        }
    }
    return best
}

fun snapAngleDegrees(degIn: Float): Float {
    val common = listOf(0f, 30f, 45f, 60f, 90f, 120f, 135f, 150f, 180f)
    var deg = ((degIn % 360f) + 360f) % 360f
    for (c in common) {
        if (kotlin.math.abs(deg - c) < 3f) return c
    }
    return kotlin.math.round(deg)
}

fun findNearbySnapPoint(p: Offset, points: List<Offset>, threshold: Float = 20f): Offset? {
    var best: Offset? = null
    var bestD2 = Float.MAX_VALUE
    for (q in points) {
        val dx = p.x - q.x
        val dy = p.y - q.y
        val d2 = dx * dx + dy * dy
        if (d2 < threshold * threshold && d2 < bestD2) {
            best = q
            bestD2 = d2
        }
    }
    return best
}

fun computeTriangle(label: String, center: Offset, angle: Float, size: Float = 400f): Array<Offset> {
    val half = size / 2f
    val local = when (label) {
        "45" -> arrayOf(
            Offset(-half, half),
            Offset(half, half),
            Offset(-half, -half)
        )
        "3060" -> {
            val h = half
            val short = h
            val long = (h / kotlin.math.sin(Math.toRadians(30.0)).toFloat()) *
                    kotlin.math.sin(Math.toRadians(60.0)).toFloat()
            arrayOf(
                Offset(-short, -h),
                Offset(long, -h),
                Offset(-short, h)
            )
        }
        "right" -> arrayOf(
            Offset(-half, -half),
            Offset(half, -half),
            Offset(-half, half)
        )
        else -> arrayOf(Offset.Zero, Offset.Zero, Offset.Zero)
    }
    val cos = kotlin.math.cos(angle)
    val sin = kotlin.math.sin(angle)
    return Array(3) { i ->
        val p = local[i]
        val rx = p.x * cos - p.y * sin
        val ry = p.x * sin + p.y * cos
        Offset(center.x + rx, center.y + ry)
    }
}

fun projectOntoTriangleEdge(p: Offset, center: Offset, angle: Float, tool: SnappyTool): Offset {
    val tri = computeTriangle("",center, angle)
    var best: Offset = tri[0]
    var bestD2 = Float.MAX_VALUE
    fun projectToSegment(a: Offset, b: Offset): Offset {
        val ab = b - a
        val ap = p - a
        val ab2 = ab.x * ab.x + ab.y * ab.y
        val t = if (ab2 == 0f) 0f else ((ap.x * ab.x + ap.y * ab.y) / ab2).coerceIn(0f, 1f)
        return a + ab * t
    }
    val edges = arrayOf(0 to 1, 1 to 2, 2 to 0)
    for ((i, j) in edges) {
        val q = projectToSegment(tri[i], tri[j])
        val dx = p.x - q.x
        val dy = p.y - q.y
        val d2 = dx * dx + dy * dy
        if (d2 < bestD2) {
            bestD2 = d2
            best = q
        }
    }
    return best
}

fun addInterpolatedPoints(target: MutableList<Offset>, from: Offset, to: Offset) {
    val dx = to.x - from.x
    val dy = to.y - from.y
    val distance = kotlin.math.hypot(dx, dy)
    if (distance <= 2f) {
        target.add(to)
        return
    }
    val steps = (distance / 2f).toInt().coerceAtLeast(1)
    val stepX = dx / steps
    val stepY = dy / steps
    var x = from.x
    var y = from.y
    for (i in 1..steps) {
        x += stepX
        y += stepY
        target.add(Offset(x, y))
    }
}

fun androidx.compose.ui.graphics.drawscope.DrawScope.drawGrid(
    step: Float = 32f,
    color: Color = Color.LightGray
) {
    val width = size.width
    val height = size.height
    val strokeWidth = 1.dp.toPx()

    var x = 0f
    while (x <= width) {
        drawLine(color = color, start = Offset(x, 0f), end = Offset(x, height), strokeWidth = strokeWidth)
        x += step
    }

    var y = 0f
    while (y <= height) {
        drawLine(color = color, start = Offset(0f, y), end = Offset(width, y), strokeWidth = strokeWidth)
        y += step
    }
}
