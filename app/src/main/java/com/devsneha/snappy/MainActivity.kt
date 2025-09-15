package com.devsneha.snappy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.devsneha.snappy.ui.theme.SnappyTheme
import com.devsneha.snappy.ui.SnappyCanvasScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SnappyTheme {
               SnappyCanvasScreen()
            }
        }
    }
}