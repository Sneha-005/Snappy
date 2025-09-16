package com.devsneha.snappy.ui

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import com.devsneha.snappy.model.DrawPath
import com.devsneha.snappy.model.SnappyTool
import com.devsneha.snappy.state.CanvasState
import com.devsneha.snappy.util.*

@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
@Composable
fun SnappyCanvas(
    modifier: Modifier = Modifier,
    state: CanvasState,
    tool: SnappyTool,
    onCanUndoChanged: (Boolean) -> Unit,
    onCanRedoChanged: (Boolean) -> Unit,
    undoSignal: Int,
    redoSignal: Int,
    clearSignal: Int
) {
    // Undo
    LaunchedEffect(undoSignal) {
        if (state.finalizedPaths.isNotEmpty()) {
            val removed = state.finalizedPaths.removeLast()
            state.redoStack.add(removed)
            onCanUndoChanged(state.finalizedPaths.isNotEmpty())
            onCanRedoChanged(state.redoStack.isNotEmpty())
        }
    }

    // Redo
    LaunchedEffect(redoSignal) {
        if (state.redoStack.isNotEmpty()) {
            val restored = state.redoStack.removeLast()
            state.finalizedPaths.add(restored)
            onCanUndoChanged(state.finalizedPaths.isNotEmpty())
            onCanRedoChanged(state.redoStack.isNotEmpty())
        }
    }

    // Clear
    LaunchedEffect(clearSignal) {
        state.finalizedPaths.clear()
        state.redoStack.clear()
        onCanUndoChanged(false)
        onCanRedoChanged(false)
    }

    Canvas(
        modifier = modifier
            .background(Color(0xFFF9F9FB))
            .pointerInput(tool) {
                when (tool) {
                    SnappyTool.Pen -> {
                        detectDragGestures(
                            onDragStart = { offset ->
                                state.currentPath.value = Path().apply { moveTo(offset.x, offset.y) }
                                state.lastPoint.value = offset
                            },
                            onDrag = { change, _ ->
                                val current = change.position
                                state.lastPoint.value?.let {
                                    state.currentPath.value?.lineTo(current.x, current.y)
                                    state.snapPoints.add(current)
                                }
                                state.lastPoint.value = current
                            },
                            onDragEnd = {
                                state.currentPath.value?.let { path ->
                                    state.finalizedPaths.add(
                                        DrawPath(path, state.currentPathColor.value, state.currentPathWidth.value)
                                    )
                                }
                                state.currentPath.value = null
                                state.lastPoint.value = null
                                onCanUndoChanged(state.finalizedPaths.isNotEmpty())
                            }
                        )
                    }
                    SnappyTool.Ruler,
                    SnappyTool.RightAngle,
                    SnappyTool.SetSquare45,
                    SnappyTool.SetSquare3060 -> {
                        detectTapGestures(
                            onPress = { pos ->
                                state.anchorStart.value = pos
                                awaitRelease()
                                state.previewEnd.value?.let { end ->
                                    val path = Path().apply {
                                        moveTo(state.anchorStart.value!!.x, state.anchorStart.value!!.y)
                                        lineTo(end.x, end.y)
                                    }
                                    state.finalizedPaths.add(
                                        DrawPath(path, state.currentPathColor.value, state.currentPathWidth.value)
                                    )
                                }
                                state.anchorStart.value = null
                                state.previewEnd.value = null
                                onCanUndoChanged(state.finalizedPaths.isNotEmpty())
                            }
                        )
                    }
                    SnappyTool.Protractor -> {
                        detectTapGestures(
                            onTap = { pos ->
                                if (state.anchorStart.value == null) {
                                    state.anchorStart.value = pos
                                } else {
                                    val start = state.anchorStart.value!!
                                    val end = pos
                                    val dx = end.x - start.x
                                    val dy = end.y - start.y
                                    state.protractorMeasure.value =
                                        Math.toDegrees(Math.atan2(dy.toDouble(), dx.toDouble())).toFloat()
                                    state.anchorStart.value = null
                                }
                            }
                        )
                    }
                    SnappyTool.Compass -> {
                        detectTapGestures(
                            onPress = { pos ->
                                state.anchorStart.value = pos
                                awaitRelease()
                                val end = state.previewEnd.value
                                if (end != null) {
                                    val r = (end - pos).getDistance()
                                    val path = Path().apply {
                                        val rect = Rect(pos.x - r, pos.y - r, pos.x + r, pos.y + r)
                                        addArc(rect, 0f, 360f)
                                    }

                                    state.finalizedPaths.add(
                                        DrawPath(path, state.currentPathColor.value, state.currentPathWidth.value)
                                    )
                                }
                                state.anchorStart.value = null
                                state.previewEnd.value = null
                                onCanUndoChanged(state.finalizedPaths.isNotEmpty())
                            }
                        )
                    }
                }
            }
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, rotation ->
                    state.viewScale.value *= zoom
                    state.viewTranslate.value += pan
                    when (tool) {
                        SnappyTool.Ruler -> state.rulerAngle.value += rotation
                        SnappyTool.RightAngle,
                        SnappyTool.SetSquare45,
                        SnappyTool.SetSquare3060 -> state.setSquareAngle.value += rotation
                        SnappyTool.Protractor -> state.protractorAngle.value += rotation
                        else -> {}
                    }
                }
            }
    ) {
        // Draw grid
        drawGrid()

        // Finalized paths
        state.finalizedPaths.forEach { dp ->
            drawPath(
                dp.path,
                color = dp.color,
                style = Stroke(dp.width, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
        }

        // Current path
        state.currentPath.value?.let { path ->
            drawPath(
                path,
                color = state.currentPathColor.value,
                style = Stroke(state.currentPathWidth.value, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
        }

        // Tool overlays
        when (tool) {
            SnappyTool.Ruler -> {
                rotate(state.rulerAngle.value, state.rulerCenter.value) {
                    drawLine(
                        color = Color.Gray,
                        start = Offset(state.rulerCenter.value.x - 400f, state.rulerCenter.value.y),
                        end = Offset(state.rulerCenter.value.x + 400f, state.rulerCenter.value.y),
                        strokeWidth = 6f
                    )
                }
            }
            SnappyTool.RightAngle -> {
                val tri = computeTriangle("right", state.setSquareCenter.value, state.setSquareAngle.value)
                drawLine(Color.Gray, tri[0], tri[1], strokeWidth = 6f)
                drawLine(Color.Gray, tri[1], tri[2], strokeWidth = 6f)
                drawLine(Color.Gray, tri[2], tri[0], strokeWidth = 6f)
            }
            SnappyTool.SetSquare45 -> {
                val tri = computeTriangle("45", state.setSquareCenter.value, state.setSquareAngle.value)
                drawLine(Color.Gray, tri[0], tri[1], strokeWidth = 6f)
                drawLine(Color.Gray, tri[1], tri[2], strokeWidth = 6f)
                drawLine(Color.Gray, tri[2], tri[0], strokeWidth = 6f)
            }
            SnappyTool.SetSquare3060 -> {
                val tri = computeTriangle("3060", state.setSquareCenter.value, state.setSquareAngle.value)
                drawLine(Color.Gray, tri[0], tri[1], strokeWidth = 6f)
                drawLine(Color.Gray, tri[1], tri[2], strokeWidth = 6f)
                drawLine(Color.Gray, tri[2], tri[0], strokeWidth = 6f)
            }
            SnappyTool.Protractor -> {
                drawArc(
                    color = Color.Gray,
                    startAngle = 0f,
                    sweepAngle = 180f,
                    useCenter = false,
                    topLeft = state.protractorCenter.value - Offset(200f, 200f),
                    size = androidx.compose.ui.geometry.Size(400f, 400f),
                    style = Stroke(width = 4f)
                )
                state.protractorMeasure.value?.let { ang ->
                    rotate(ang, state.protractorCenter.value) {
                        drawLine(
                            Color.Red,
                            state.protractorCenter.value,
                            state.protractorCenter.value + Offset(200f, 0f),
                            strokeWidth = 4f
                        )
                    }
                }
            }
            SnappyTool.Compass -> {
                val r = state.compassRadius.value
                drawCircle(Color.Gray, radius = r, center = state.compassCenter.value, style = Stroke(width = 4f))
            }
            else -> {}
        }
    }
}
