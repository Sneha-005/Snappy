package com.devsneha.snappy

import androidx.compose.ui.geometry.Offset
import com.devsneha.snappy.ui.SnappyTool

data class InteractionState(
    var selectedTool: SnappyTool = SnappyTool.Pen,
    var lastPoint: Offset? = null,
    var anchorStart: Offset? = null,
    var previewEnd: Offset? = null,
    var rulerCenter: Offset = Offset(300f, 300f),
    var rulerAngle: Float = 0f,
    var setSquareCenter: Offset = Offset(600f, 300f),
    var setSquareAngle: Float = 0f,
    var protractorCenter: Offset = Offset(300f, 600f),
    var protractorAngle: Float = 0f,
    var compassCenter: Offset = Offset(600f, 600f),
    var compassRadius: Float = 120f,
    var viewScale: Float = 1f,
    var viewTranslate: Offset = Offset.Zero
)