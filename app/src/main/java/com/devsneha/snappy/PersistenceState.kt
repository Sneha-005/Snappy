package com.devsneha.snappy

import com.devsneha.snappy.ui.DrawPath

data class PersistenceState(
    val undoStack: MutableList<DrawPath> = mutableListOf(),
    val redoStack: MutableList<DrawPath> = mutableListOf(),
    var undoSignal: Int = 0,
    var redoSignal: Int = 0,
    var clearSignal: Int = 0
)