package com.devsneha.snappy.state

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.geometry.Offset
import com.devsneha.snappy.model.DrawPath

class CanvasState {
    val finalizedPaths = mutableStateListOf<DrawPath>()
    val undoStack = mutableStateListOf<DrawPath>()
    val redoStack = mutableStateListOf<DrawPath>()

    var currentPath = mutableStateOf<androidx.compose.ui.graphics.Path?>(null)
    var currentPathColor = mutableStateOf(androidx.compose.ui.graphics.Color.Black)
    var currentPathWidth = mutableStateOf(6f)

    var lastPoint = mutableStateOf<Offset?>(null)
    var anchorStart = mutableStateOf<Offset?>(null)
    var previewEnd = mutableStateOf<Offset?>(null)

    var snapPoints = mutableStateListOf<Offset>()
    var snapHint = mutableStateOf<Offset?>(null)

    var rulerCenter = mutableStateOf(Offset(300f, 300f))
    var rulerAngle = mutableStateOf(0f)

    var setSquareCenter = mutableStateOf(Offset(600f, 300f))
    var setSquareAngle = mutableStateOf(0f)

    var protractorCenter = mutableStateOf(Offset(300f, 600f))
    var protractorAngle = mutableStateOf(0f)
    var protractorMeasure = mutableStateOf<Float?>(null)

    var compassCenter = mutableStateOf(Offset(600f, 600f))
    var compassRadius = mutableStateOf(120f)

    var viewScale = mutableStateOf(1f)
    var viewTranslate = mutableStateOf(Offset.Zero)

    var frameTick = mutableStateOf(0)
}
