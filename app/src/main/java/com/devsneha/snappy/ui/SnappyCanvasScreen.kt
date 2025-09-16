package com.devsneha.snappy.ui

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.devsneha.snappy.model.SnappyTool
import com.devsneha.snappy.state.CanvasState

@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
@Composable
fun SnappyCanvasScreen() {
    val canvasState = remember { CanvasState() }

    var canUndo by remember { mutableStateOf(false) }
    var canRedo by remember { mutableStateOf(false) }
    var selectedTool by remember { mutableStateOf(SnappyTool.Pen) }

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
                    .align(Alignment.BottomEnd)
                    .zIndex(2f)
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                ToolChip("Undo", enabled = canUndo) { canvasState.frameTick.value++ }
                ToolChip("Redo", enabled = canRedo) { canvasState.frameTick.value++ }
                ToolChip("Clear", enabled = true) { canvasState.frameTick.value++ }
            }

            SnappyCanvas(
                modifier = Modifier.fillMaxSize(),
                state = canvasState,
                tool = selectedTool,
                onCanUndoChanged = { canUndo = it },
                onCanRedoChanged = { canRedo = it },
                undoSignal = 0,
                redoSignal = 0,
                clearSignal = 0
            )
        }
    }
}
