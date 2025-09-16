package com.devsneha.snappy.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path

data class DrawPath(
    val path: Path,
    val color: Color = Color.Black,
    val width: Float = 6f
)
