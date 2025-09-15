package com.devsneha.snappy

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import com.devsneha.snappy.ui.DrawPath
import java.nio.file.Path

data class RenderState(
    val finalizedPaths: MutableList<DrawPath> = mutableListOf(),
    val currentPath: Path? = null,
    val currentPathColor: Color = Color.Black,
    val currentPathWidth: Float = 6f,
    val snapPoints: MutableList<Offset> = mutableListOf()
)
