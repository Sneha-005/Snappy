package com.devsneha.snappy.ui

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex

enum class SnappyTool {
    Pen,
    Ruler,
    RightAngle,
    SetSquare45,
    SetSquare3060,
    Protractor,
    Compass
}

data class DrawPath(
    val path: Path,
    val color: Color = Color.Black,
    val width: Float = 6f
)

@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
@Composable
fun SnappyCanvasScreen() {
    var selectedTool by remember { mutableStateOf(SnappyTool.Pen) }
    var canUndo by remember { mutableStateOf(false) }
    var canRedo by remember { mutableStateOf(false) }
    var undoSignal by remember { mutableStateOf(0) }
    var redoSignal by remember { mutableStateOf(0) }
    var clearSignal by remember { mutableStateOf(0) }

    Surface(color = MaterialTheme.colorScheme.background) {
        Box(modifier = Modifier.fillMaxSize()) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .padding(12.dp)
                    .zIndex(1f)
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                item { ToolChip("Pen", selectedTool == SnappyTool.Pen) { selectedTool = SnappyTool.Pen } }
                item { ToolChip("Ruler", selectedTool == SnappyTool.Ruler) { selectedTool = SnappyTool.Ruler } }
                item { ToolChip("Right ⟂", selectedTool == SnappyTool.RightAngle) { selectedTool = SnappyTool.RightAngle } }
                item { ToolChip("Set 45°", selectedTool == SnappyTool.SetSquare45) { selectedTool = SnappyTool.SetSquare45 } }
                item { ToolChip("Set 30/60°", selectedTool == SnappyTool.SetSquare3060) { selectedTool = SnappyTool.SetSquare3060 } }
                item { ToolChip("Protractor", selectedTool == SnappyTool.Protractor) { selectedTool = SnappyTool.Protractor } }
                item { ToolChip("Compass", selectedTool == SnappyTool.Compass) { selectedTool = SnappyTool.Compass } }
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .padding(12.dp)
                    .align(androidx.compose.ui.Alignment.BottomEnd)
                    .zIndex(2f)
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                ToolChip("Undo", enabled = canUndo) { undoSignal += 1 }
                ToolChip("Redo", enabled = canRedo) { redoSignal += 1 }
                ToolChip("Clear", enabled = true) { clearSignal += 1 }
            }
            SnappyCanvas(
                modifier = Modifier.fillMaxSize(),
                tool = selectedTool,
                onCanUndoChanged = { canUndo = it },
                onCanRedoChanged = { canRedo = it },
                undoSignal = undoSignal,
                redoSignal = redoSignal,
                clearSignal = clearSignal
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
@Composable
fun SnappyCanvas(
    modifier: Modifier = Modifier,
    tool: SnappyTool = SnappyTool.Pen,
    onCanUndoChanged: (Boolean) -> Unit = {},
    onCanRedoChanged: (Boolean) -> Unit = {},
    undoSignal: Int = 0,
    redoSignal: Int = 0,
    clearSignal: Int = 0,
) {
    val finalizedPaths = remember { mutableStateListOf<DrawPath>() }
    var currentPath by remember { mutableStateOf<Path?>(null) }
    var currentPathColor by remember { mutableStateOf(Color.Black) }
    var currentPathWidth by remember { mutableStateOf(6f) }
    var lastPoint by remember { mutableStateOf<Offset?>(null) }
    var anchorStart by remember { mutableStateOf<Offset?>(null) }
    var previewEnd by remember { mutableStateOf<Offset?>(null) }
    var lastUndoSignal by remember { mutableStateOf(0) }
    var lastRedoSignal by remember { mutableStateOf(0) }
    var lastClearSignal by remember { mutableStateOf(0) }
    var frameTick by remember { mutableStateOf(0) }
    val snapPoints = remember { mutableStateListOf<Offset>() }
    var rulerCenter by remember { mutableStateOf(Offset(300f, 300f)) }
    var rulerAngle by remember { mutableStateOf(0f) }
    var setSquareCenter by remember { mutableStateOf(Offset(600f, 300f)) }
    var setSquareAngle by remember { mutableStateOf(0f) }
    var protractorCenter by remember { mutableStateOf(Offset(300f, 600f)) }
    var protractorAngle by remember { mutableStateOf(0f) }
    var protractorMeasure by remember { mutableStateOf<Float?>(null) }
    var compassCenter by remember { mutableStateOf(Offset(600f, 600f)) }
    var compassRadius by remember { mutableStateOf(120f) }
    var viewScale by remember { mutableStateOf(1f) }
    var viewTranslate by remember { mutableStateOf(Offset.Zero) }
    var snapHint: Offset? by remember { mutableStateOf(null) }
    val undoStack = remember { mutableStateListOf<DrawPath>() }
    val redoStack = remember { mutableStateListOf<DrawPath>() }

    if (undoSignal != lastUndoSignal) {
        val removed = finalizedPaths.removeLastOrNull()
        if (removed != null) {
            redoStack.add(removed)
        }
        lastUndoSignal = undoSignal
        onCanUndoChanged(finalizedPaths.isNotEmpty())
        onCanRedoChanged(redoStack.isNotEmpty())
    }
    if (redoSignal != lastRedoSignal) {
        val restored = redoStack.removeLastOrNull()
        if (restored != null) {
            finalizedPaths.add(restored)
        }
        lastRedoSignal = redoSignal
        onCanUndoChanged(finalizedPaths.isNotEmpty())
        onCanRedoChanged(redoStack.isNotEmpty())
    }
    if (clearSignal != lastClearSignal) {
        finalizedPaths.clear()
        redoStack.clear()
        currentPath = null
        lastPoint = null
        anchorStart = null
        previewEnd = null
        lastClearSignal = clearSignal
        onCanUndoChanged(finalizedPaths.isNotEmpty())
        onCanRedoChanged(redoStack.isNotEmpty())
    }

    Canvas(
        modifier = modifier
            .background(Color(0xFFF9F9FB))
            .pointerInput(tool, viewScale, viewTranslate) {
                if (tool == SnappyTool.Ruler || tool == SnappyTool.SetSquare45 || tool == SnappyTool.SetSquare3060) {
                    detectTransformGestures(
                        onGesture = { centroid, pan, zoom, rotation ->
                            if (zoom != 1f) {
                                val newScale = (viewScale * zoom).coerceIn(0.3f, 5f)
                                val focus = centroid
                                val before = (focus - viewTranslate) / viewScale
                                viewScale = newScale
                                viewTranslate = focus - before * viewScale
                            } else if (pan != Offset.Zero) {
                                viewTranslate += pan
                            }
                            if (tool == SnappyTool.Ruler) {
                                rulerCenter += pan
                                rulerAngle = snapAngle(rulerAngle + rotation)
                            } else {
                                setSquareCenter += pan
                                setSquareAngle += rotation
                            }
                            frameTick++
                        }
                    )
                } else if (tool == SnappyTool.Protractor) {
                    detectTransformGestures(
                        onGesture = { centroid, pan, zoom, rotation ->
                            if (zoom != 1f) {
                                val newScale = (viewScale * zoom).coerceIn(0.3f, 5f)
                                val focus = centroid
                                val before = (focus - viewTranslate) / viewScale
                                viewScale = newScale
                                viewTranslate = focus - before * viewScale
                            } else if (pan != Offset.Zero) {
                                viewTranslate += pan
                            }
                            protractorCenter += pan
                            protractorAngle += rotation
                            frameTick++
                        }
                    )
                } else if (tool == SnappyTool.Compass) {
                    detectTransformGestures(
                        onGesture = { centroid, pan, zoom, _ ->
                            if (zoom != 1f) {
                                val newScale = (viewScale * zoom).coerceIn(0.3f, 5f)
                                val focus = centroid
                                val before = (focus - viewTranslate) / viewScale
                                viewScale = newScale
                                viewTranslate = focus - before * viewScale
                            } else if (pan != Offset.Zero) {
                                viewTranslate += pan
                            }
                            compassCenter += pan
                            compassRadius = (compassRadius * zoom).coerceIn(10f, 2000f)
                            frameTick++
                        }
                    )
                }
            }
            .pointerInput(tool) {
                detectDragGestures(
                    onDragStart = { position ->
                        when (tool) {
                            SnappyTool.Pen -> {
                                currentPath = Path().apply { moveTo(position.x, position.y) }
                                currentPathColor = Color.Black
                                currentPathWidth = 6f
                                lastPoint = position
                                frameTick++
                            }
                            SnappyTool.Ruler -> {
                                val projected = projectOntoRuler(position, rulerCenter, rulerAngle)
                                anchorStart = findNearbySnapPoint(projected, snapPoints) ?: projected
                                previewEnd = anchorStart
                                frameTick++
                            }
                            SnappyTool.RightAngle -> {
                                if (anchorStart == null) {
                                    anchorStart = snapPoint(position, null)
                                }
                                previewEnd = snapPoint(position, anchorStart)
                            }
                            SnappyTool.SetSquare45, SnappyTool.SetSquare3060 -> {
                                val projected = projectOntoTriangleEdge(position, setSquareCenter, setSquareAngle, tool)
                                anchorStart = findNearbySnapPoint(projected, snapPoints) ?: projected
                                previewEnd = anchorStart
                                frameTick++
                            }
                            SnappyTool.Protractor -> {
                                protractorMeasure = 0f
                                frameTick++
                            }
                            SnappyTool.Compass -> {
                                compassCenter = position
                                frameTick++
                            }
                        }
                    },
                    onDrag = { change, _ ->
                        when (tool) {
                            SnappyTool.Pen -> {
                                val lp = lastPoint
                                val path = currentPath
                                if (lp != null && path != null) {
                                    val dx = change.position.x - lp.x
                                    val dy = change.position.y - lp.y
                                    val d2 = dx * dx + dy * dy
                                    if (d2 > 4.0f) {
                                        val midX = (lp.x + change.position.x) / 2f
                                        val midY = (lp.y + change.position.y) / 2f
                                        path.quadraticBezierTo(lp.x, lp.y, midX, midY)
                                        lastPoint = change.position
                                        frameTick++
                                    }
                                }
                            }
                            SnappyTool.Ruler -> {
                                val start = anchorStart
                                if (start != null) {
                                    val projected = projectOntoRuler(change.position, rulerCenter, rulerAngle)
                                    val snapped = findNearbySnapPoint(projected, snapPoints) ?: projected
                                    previewEnd = snapped
                                    frameTick++
                                }
                            }
                            SnappyTool.RightAngle -> {
                                val start = anchorStart
                                if (start != null) {
                                    previewEnd = snapConstrainedLine(change.position, start, constrainPerpendicular = true)
                                    frameTick++
                                }
                            }
                            SnappyTool.SetSquare45, SnappyTool.SetSquare3060 -> {
                                val start = anchorStart
                                if (start != null) {
                                    val projected = projectOntoTriangleEdge(change.position, setSquareCenter, setSquareAngle, tool)
                                    val snapped = findNearbySnapPoint(projected, snapPoints) ?: projected
                                    previewEnd = snapped
                                    frameTick++
                                }
                            }
                            SnappyTool.Protractor -> {
                                val baseline = protractorAngle
                                val vec = change.position - protractorCenter
                                var ang = kotlin.math.atan2(vec.y, vec.x) - baseline
                                val twoPi = (Math.PI * 2).toFloat()
                                while (ang < 0) ang += twoPi
                                while (ang > twoPi) ang -= twoPi
                                val deg = Math.toDegrees(ang.toDouble()).toFloat()
                                protractorMeasure = snapAngleDegrees(deg)
                                frameTick++
                            }
                            SnappyTool.Compass -> {
                                compassRadius = (change.position - compassCenter).getDistance()
                                val near = findNearbySnapPoint(change.position, snapPoints, 20f)
                                if (near != null) {
                                    compassRadius = (near - compassCenter).getDistance()
                                }
                                frameTick++
                            }
                        }
                    },
                    onDragEnd = {
                        when (tool) {
                            SnappyTool.Pen -> {
                                val path = currentPath
                                if (path != null) {
                                    finalizedPaths.add(DrawPath(path = path, color = currentPathColor, width = currentPathWidth))
                                }
                                currentPath = null
                                lastPoint = null
                                frameTick++
                            }
                            SnappyTool.Ruler, SnappyTool.RightAngle, SnappyTool.SetSquare45, SnappyTool.SetSquare3060 -> {
                                val start = anchorStart
                                val end = previewEnd
                                if (start != null && end != null) {
                                    val path = Path().apply {
                                        moveTo(start.x, start.y)
                                        lineTo(end.x, end.y)
                                    }
                                    finalizedPaths.add(DrawPath(path = path, color = Color.Black, width = 6f))
                                    snapPoints.add(start)
                                    snapPoints.add(end)
                                    val mid = Offset((start.x + end.x) / 2f, (start.y + end.y) / 2f)
                                    snapPoints.add(mid)
                                }
                                anchorStart = null
                                previewEnd = null
                                frameTick++
                            }
                            SnappyTool.Compass -> {
                                val r = compassRadius
                                if (r > 2f) {
                                    val circle = Path().apply {
                                        addArc(
                                            Rect(
                                                left = compassCenter.x - r,
                                                top = compassCenter.y - r,
                                                right = compassCenter.x + r,
                                                bottom = compassCenter.y + r
                                            ), 0f, 360f)
                                    }
                                    finalizedPaths.add(DrawPath(path = circle, color = Color.Black, width = 6f))
                                }
                                frameTick++
                            }

                            SnappyTool.Protractor -> {
                                val measure = protractorMeasure
                                if (measure != null) {
                                    val arc = Path().apply {
                                        addArc(
                                            Rect(
                                                left = protractorCenter.x - 100f,
                                                top = protractorCenter.y - 100f,
                                                right = protractorCenter.x + 100f,
                                                bottom = protractorCenter.y + 100f
                                            ),
                                            Math.toDegrees(protractorAngle.toDouble()).toFloat(),
                                            measure
                                        )
                                    }
                                    finalizedPaths.add(DrawPath(path = arc, color = Color.Black, width = 6f))
                                }
                                frameTick++
                            }
                        }
                        onCanUndoChanged(finalizedPaths.isNotEmpty())
                        onCanRedoChanged(redoStack.isNotEmpty())
                    },
                    onDragCancel = {
                        currentPath = null
                        lastPoint = null
                        previewEnd = null
                        anchorStart = null
                        frameTick++
                    }
                )
            }
    ) {
        val f = frameTick
        drawGrid(color = Color(0xFFE6E8EE))

        if (tool == SnappyTool.Ruler) {
            val halfLen = maxOf(size.width, size.height) * 0.75f
            val dir = Offset(kotlin.math.cos(rulerAngle), kotlin.math.sin(rulerAngle))
            val p1 = rulerCenter - dir * halfLen
            val p2 = rulerCenter + dir * halfLen
            drawLine(
                color = Color(0x802196F3),
                start = p1,
                end = p2,
                strokeWidth = 6.dp.toPx()
            )
        }

        if (tool == SnappyTool.SetSquare45 || tool == SnappyTool.SetSquare3060) {
            val tri = computeTriangle(setSquareCenter, setSquareAngle, tool, size.minDimension * 0.5f)
            drawLine(color = Color(0x802196F3), start = tri[0], end = tri[1], strokeWidth = 4.dp.toPx())
            drawLine(color = Color(0x802196F3), start = tri[1], end = tri[2], strokeWidth = 4.dp.toPx())
            drawLine(color = Color(0x802196F3), start = tri[2], end = tri[0], strokeWidth = 4.dp.toPx())
        }

        if (tool == SnappyTool.Protractor) {
            val r = size.minDimension * 0.3f
            drawArc(
                color = Color(0x332196F3),
                startAngle = Math.toDegrees(protractorAngle.toDouble()).toFloat(),
                sweepAngle = 180f,
                useCenter = true,
                topLeft = Offset(protractorCenter.x - r, protractorCenter.y - r),
                size = androidx.compose.ui.geometry.Size(r * 2, r * 2)
            )
            val baseDir = Offset(kotlin.math.cos(protractorAngle), kotlin.math.sin(protractorAngle))
            drawLine(color = Color(0xFF2196F3), start = protractorCenter, end = protractorCenter + baseDir * r, strokeWidth = 3.dp.toPx())
            val ang = protractorMeasure ?: 0f
            val rad = Math.toRadians(ang.toDouble()).toFloat() + protractorAngle
            val ray = Offset(kotlin.math.cos(rad), kotlin.math.sin(rad))
            drawLine(color = Color(0xFF1565C0), start = protractorCenter, end = protractorCenter + ray * r, strokeWidth = 3.dp.toPx())
            drawContext.canvas.nativeCanvas.apply {
                val text = "${ang.toInt()}°"
                val paint = android.graphics.Paint().apply {
                    color = android.graphics.Color.BLACK
                    textSize = 36f
                    isAntiAlias = true
                }
                drawText(text, protractorCenter.x + 12f, protractorCenter.y - 12f, paint)
            }
        }

        if (tool == SnappyTool.Compass) {
            drawCircle(color = Color(0x332196F3), radius = compassRadius, center = compassCenter)
            drawCircle(color = Color(0xFF2196F3), radius = 4.dp.toPx(), center = compassCenter)
        }

        finalizedPaths.forEach { dp ->
            drawPath(
                path = dp.path,
                color = dp.color,
                style = Stroke(
                    width = dp.width,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )
        }

        val live = currentPath
        if (live != null) {
            drawPath(
                path = live,
                color = currentPathColor,
                style = Stroke(
                    width = currentPathWidth,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )
        }
        val hint = snapHint
        if (hint != null) {
            drawCircle(color = Color(0xFF00C853), radius = 5.dp.toPx(), center = hint)
        }
        val start = anchorStart
        val end = previewEnd
        if (start != null && end != null) {
            val dx = end.x - start.x
            val dy = end.y - start.y
            val length = kotlin.math.hypot(dx.toDouble(), dy.toDouble()).toFloat()
            val angle = Math.toDegrees(kotlin.math.atan2(dy.toDouble(), dx.toDouble())).toFloat()
            val text = "${length.toInt()}px  ${angle.toInt()}°"
            drawContext.canvas.nativeCanvas.apply {
                val paint = android.graphics.Paint().apply {
                    color = android.graphics.Color.DKGRAY
                    textSize = 28f
                    isAntiAlias = true
                }
                drawText(text, end.x + 12f, end.y - 12f, paint)
            }
        }
        if (start != null && end != null) {
            drawLine(
                color = Color(0xFF1565C0),
                start = start,
                end = end,
                strokeWidth = 3.dp.toPx()
            )
        }
    }
}

private fun snapPoint(p: Offset, last: Offset?): Offset {
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

private fun snapConstrainedLine(p: Offset, start: Offset, constrainPerpendicular: Boolean): Offset {
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

private fun projectOntoRuler(p: Offset, center: Offset, angle: Float): Offset {
    val dir = Offset(kotlin.math.cos(angle), kotlin.math.sin(angle))
    val v = p - center
    val t = (v.x * dir.x + v.y * dir.y)
    return center + dir * t
}

private fun snapAngle(angle: Float): Float {
    val allowed = floatArrayOf(0f, 30f, 45f, 60f, 90f)
    val twoPi = (Math.PI * 2).toFloat()
    var a = angle % twoPi
    if (a < 0) a += twoPi
    val deg = Math.toDegrees(a.toDouble()).toFloat()
    // consider all multiples of allowed up to 360
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

private fun snapAngleDegrees(degIn: Float): Float {
    val common = listOf(0f, 30f, 45f, 60f, 90f, 120f, 135f, 150f, 180f)
    var deg = ((degIn % 360f) + 360f) % 360f
    for (c in common) {
        if (kotlin.math.abs(deg - c) < 3f) return c
    }
    return kotlin.math.round(deg)
}

private fun findNearbySnapPoint(p: Offset, points: List<Offset>, threshold: Float = 20f): Offset? {
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

private fun computeTriangle(center: Offset, angle: Float, tool: SnappyTool, size: Float): Array<Offset> {
    val half = size / 2f
    val local = when (tool) {
        SnappyTool.SetSquare45 -> arrayOf(
            Offset(-half, half),
            Offset(half, half),
            Offset(-half, -half)
        )
        SnappyTool.SetSquare3060 -> {
            val h = half
            val short = h
            val long = (h / kotlin.math.sin(Math.toRadians(30.0)).toFloat()) * kotlin.math.sin(Math.toRadians(60.0)).toFloat()
            arrayOf(
                Offset(-short, -h),
                Offset(long, -h),
                Offset(-short, h)
            )
        }
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

private fun projectOntoTriangleEdge(p: Offset, center: Offset, angle: Float, tool: SnappyTool): Offset {
    val tri = computeTriangle(center, angle, tool, size = 400f)
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

private fun addInterpolatedPoints(target: MutableList<Offset>, from: Offset, to: Offset) {
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

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawGrid(
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

@Composable
private fun ToolChip(label: String, selected: Boolean = false, enabled: Boolean = true, onClick: () -> Unit) {
    androidx.compose.material3.OutlinedButton(
        enabled = enabled,
        onClick = onClick,
        border = if (selected) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
        modifier = Modifier.height(36.dp)
    ) {
        Text(
            text = label,
            color = if (enabled) MaterialTheme.colorScheme.onSurface else Color.Gray,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        )
    }
}


