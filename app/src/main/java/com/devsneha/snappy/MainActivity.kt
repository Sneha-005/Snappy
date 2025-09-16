package com.devsneha.snappy

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import com.devsneha.snappy.ui.theme.SnappyTheme
import com.devsneha.snappy.ui.SnappyCanvasScreen

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SnappyTheme {
               SnappyCanvasScreen()
            }
        }
    }
}